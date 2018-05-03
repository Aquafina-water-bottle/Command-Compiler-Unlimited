if __name__ == "__main__":
    import logging_setup

import os
import logging

from token_classes import SimpleToken, WhitespaceSimpleToken, StatementSimpleToken, SelectorSimpleToken, ExecuteSimpleToken
from token_classes import TypedToken, SelectorTypedToken, TokenValues
# from scoped_symbol_table import ScopedSymbolTable
# from in_file_config import InFileConfig
from nodes import ProgramNode, McFunctionNode, FolderNode, PrefixNode, ConstObjNode, CommandNode, NodeVisitor
from coord_utils import is_coord_token
from config_data import ConfigData

"""
Organizes all lines into their respective mcfunctions

program ::= statement_suite
statement_suite ::= [statement, NEWLINE]+

statement ::= "!" && [folder_stmt, mfunc_stmt, prefix_stmt, constobj_stmt]
folder_stmt ::= "folder" && STR && (NEWLINE)* & INDENT && statement_suite && DEDENT
mfunc_stmt ::= "mfunc" && STR && (NEWLINE)* & INDENT && command_suite && DEDENT
prefix_stmt ::= "prefix" && STR
constobj_stmt ::= "constobj" && STR

command_suite ::= [command, NEWLINE]+
command ::= (execute_cmd && ":")? && [scoreboard_cmd, function_cmd, simple_cmd]

execute_cmd ::= selector && (vec3)? && exec_if && (execute_cmd)?
exec_if ::= "if" && "(" && (exec_if_args)+ && ")"
exec_if_args ::= block && (vec3)? | (block && (vec3)? && ("," && exec_if_args)*)

scoreboard_cmd ::= [scoreboard_players_math, scoreboard_players_special, scoreboard_players_op, scoreboard_teams, scoreboard_tags]
scoreboard_players_math ::= [SELECTOR, STR] && STR && ["+", "-", "="] && INT && (NBT)?
scoreboard_players_special ::= [SELECTOR, STR] && ["enable", "reset", "get"] && STR
scoreboard_players_op ::= [SELECTOR, STR] && STR && ["=", "+", "-", "*", "/", "%"] && ((STR) | (SELECTOR) | 
scoreboard_teams ::= ("join" && STR && [SELECTOR, STR]+) | ("leave" && [SELECTOR, STR]+) | ("empty" && STR)
scoreboard_tags ::= [SELECTOR, STR] && ("+", "-") && STR

function_cmd ::= "function" && STR
simple_cmd ::= COMMAND_KEYWORD && (STR)*

selector ::= SELECTOR_VAR & ("[" & selector_args & "]")?

selector_args ::= (single_arg)? | (single_arg & ("," & single_arg)*)?
single_arg ::= [simple_arg, range_arg, tag_arg]

simple_arg ::= DEFAULT_ARG & "=" & ("!")? & [STRING, INT]
tag_arg ::= STRING
range_arg ::= STRING & ("=" & range)?
range ::= [INTEGER, (INTEGER & ".."), (".." & INTEGER), (INTEGER & ".." & INTEGER)]

vec3 ::= coord && coord && coord
coord ::= ("^", "~")? & ("-")? & [INT, FLOAT]
"""

class Parser:
    """
    Makes the mcfunction builders while building all datatags and selectors

    Attributes:
        lexer (Lexer)
        symbol_table (ScopedSymbolTable): Stores all symbols acquired dealing with statements and statement scopes
        file_path (str): Main output file path of the mcfunctions
        mcfunctions (list): All mcfunction builders made by the parser
    """

    config_data = ConfigData()

    def __init__(self, lexer, file_path):
        self.iterator = iter(lexer)
        self.current_token = None
        self.advance()

        # self.symbol_table = ScopedSymbolTable()
        # self.in_file_config = InFileConfig()
        # self.file_path = file_path
        # self.mcfunctions = []

    def parse(self):
        """
        Returns:
            ProgramNode: The parse tree parent
        """
        # logging.debug("Original symbol table: {}".format(repr(self.symbol_table)))
        parse_tree = self.program()
        # logging.debug("Final symbol table: {}".format(repr(self.symbol_table)))
        logging.debug("")

        return parse_tree

    def error(self, message=None):
        if message is None:
            message = "Invalid syntax"
        raise SyntaxError("{}: {}".format(self.current_token, message))

    def advance(self):
        """
        Gets the next token from the lexer without checking any type
        """
        self.current_token = next(self.iterator, None)
        logging.debug("Advanced to {}".format(repr(self.current_token)))

    def eat(self, token_type, error_message=None):
        """
        Advances given the token type and values match up with the current token

        Args:
            token_type (any token type)

        Returns:
            Token: The token that was just 'eaten'
        """

        if self.current_token.matches(token_type):
            token = self.current_token
            self.advance()
            return token

        if error_message is None:
            error_message = "Expected {}".format(token_type)
        self.error(error_message)

    def program(self):
        """
        program ::= (statement)*

        Returns:
            ProgramNode: The parse tree parent
        """
        logging.debug("Begin program at {}".format(repr(self.current_token)))
        statement_nodes = self.statement_suite()
        logging.debug("End program at {}".format(repr(self.current_token)))

        program = ProgramNode(statement_nodes)
        return program

    def statement_suite(self):
        """
        statement_suite ::= [NEWLINE, statement]*
        """
        logging.debug("Begin statement compound at {}".format(repr(self.current_token)))
        # logging.debug("with scoped symbol table = {}".format(repr(self.symbol_table)))

        statement_nodes = []

        # note that this is essentially a do-while since it never starts out as a newline
        while self.current_token.matches_any_of(WhitespaceSimpleToken.NEWLINE, StatementSimpleToken.STATEMENT_SPECIFIER):
            # base case is if a newline is not met
            if self.current_token.matches(WhitespaceSimpleToken.NEWLINE):
                self.eat(WhitespaceSimpleToken.NEWLINE)

            elif self.current_token.matches(StatementSimpleToken.STATEMENT_SPECIFIER):
                statement_node = self.statement()
                statement_nodes.append(statement_node)

            else:
                self.error("Expected a newline or statement specifier")

        logging.debug("End statement compound at {}".format(repr(self.current_token)))

        return statement_nodes

    def command_suite(self):
        """
        command_suite ::= [NEWLINE, command]*
        """
        logging.debug("Begin command compound at {}".format(repr(self.current_token)))
        # logging.debug("with scoped symbol table = {}".format(repr(self.symbol_table)))

        command_nodes = []

        # note that this is essentially a do-while since it never starts out as a newline
        while self.current_token.matches_any_of(WhitespaceSimpleToken.NEWLINE, TypedToken.STRING, SelectorTypedToken.SELECTOR_VARIABLE):
            # base case is if a newline is not met
            if self.current_token.matches(WhitespaceSimpleToken.NEWLINE):
                self.eat(WhitespaceSimpleToken.NEWLINE)

            elif self.current_token.matches_any_of(TypedToken.STRING, SelectorTypedToken.SELECTOR_VARIABLE):
                command_node = self.command()
                command_nodes.append(command_node)

            else:
                self.error("Expected a newline, command or statement specifier")

        logging.debug("End command compound at {}".format(repr(self.current_token)))

        return command_nodes

    def statement(self):
        """
        Handles any post-processor statements

        statement ::= "!" && [folder_stmt, mfunc_stmt, prefix_stmt, constobj_stmt]

        Returns:
            McFunctionNode: if the statement was the beginning of a mcfunction declaration
            FolderNode: if the statement was a folder declaration
            PrefixNode: if the statement was a prefix statement
            ConstObjNode: if the statement was a constobj statement
        """
        # all here should start with "!"
        self.eat(StatementSimpleToken.STATEMENT_SPECIFIER)

        if self.current_token.matches(StatementSimpleToken.MFUNC):
            return self.mfunc_stmt()
        if self.current_token.matches(StatementSimpleToken.FOLDER):
            return self.folder_stmt()
        if self.current_token.matches(StatementSimpleToken.PREFIX):
            return self.prefix_stmt()
        if self.current_token.matches(StatementSimpleToken.CONSTOBJ):
            return self.constobj_stmt()

        self.error("Invalid statement")

    def mfunc_stmt(self):
        """
        mfunc_stmt ::= "mfunc" && STR && (NEWLINE)* & INDENT && suite && DEDENT

        Returns:
            McFunctionNode: The mcfunction node to define the mcfunction in the parse tree
        """
        self.eat(StatementSimpleToken.MFUNC)

        # if self.symbol_table.function is not None:
        #     self.error("Cannot define a mcfunction inside an mcfunction")

        mfunc_token = self.eat(TypedToken.STRING, error_message="Expected a string after a mfunc statement")

        name = mfunc_token.value + ".mcfunction"
        # if self.symbol_table.folders is None:
        #     full_path = os.path.join(self.file_path, name)
        # else:
        #     full_path = os.path.join(self.file_path, self.symbol_table.folders, name)

        # skips any and all newlines right after a mfunc statement
        while self.current_token.matches(WhitespaceSimpleToken.NEWLINE):
            self.eat(WhitespaceSimpleToken.NEWLINE)

        self.eat(WhitespaceSimpleToken.INDENT)
        command_nodes = self.command_suite()
        self.eat(WhitespaceSimpleToken.DEDENT)

        mcfunction_node = McFunctionNode(name, command_nodes)
        return mcfunction_node

    def folder_stmt(self):
        """
        folder_stmt ::= "folder" && STR && (NEWLINE)* & INDENT && suite && DEDENT
        """
        self.eat(StatementSimpleToken.FOLDER)

        # requires there to be no current mcfunction since a folder statement
        # always occurs outside a mfunc statement
        # if self.symbol_table.function is not None:
        #     self.error("Cannot parse a folder statement inside an mcfunction")

        # sets the current folder
        # self.symbol_table = ScopedSymbolTable(enclosing_scope=self.symbol_table)
        # self.symbol_table.add_folder(folder_token.value)
        folder_token = self.eat(TypedToken.STRING)

        # skips any and all newlines right after a folder statement
        while self.current_token.matches(WhitespaceSimpleToken.NEWLINE):
            self.eat(WhitespaceSimpleToken.NEWLINE)

        self.eat(WhitespaceSimpleToken.INDENT)
        statement_nodes = self.statement_suite()
        self.eat(WhitespaceSimpleToken.DEDENT)

        # resets the current folder
        # self.symbol_table = self.symbol_table.enclosing_scope

        folder_node = FolderNode(folder_token.value, statement_nodes)
        return folder_node

    def prefix_stmt(self):
        """
        prefix_stmt ::= "prefix" && STR
        """
        self.eat(StatementSimpleToken.PREFIX)
        prefix_token = self.eat(TypedToken.STRING, error_message="Expected a string after a prefix statement")

        # requires the prefix to be defined in the global scope
        # if not self.symbol_table.is_global:
        #     self.error("Cannot define a prefix when the scope is not global")

        # self.in_file_config.prefix = prefix_token
        prefix_node = PrefixNode(prefix_token.value)
        return prefix_node

    def constobj_stmt(self):
        """
        constobj_stmt ::= "constobj" && STR
        """
        self.eat(StatementSimpleToken.CONSTOBJ)
        constobj_token = self.eat(TypedToken.STRING, error_message="Expected a string after a constobj statement")

        # requires the constobj to be defined in the global scope
        # if not self.symbol_table.is_global:
        #     self.error("The constobj cannot be set outside the global context")

        # self.in_file_config.constobj = constobj_token
        constobj_node = ConstObjNode(constobj_token.value)
        return constobj_node

    def command(self):
        """
        command ::= (execute_cmd && ":")? && [scoreboard_cmd, function_cmd, simple_cmd]

        Returns:
            str: The full built command
        """
        command_segment_nodes = []

        # gets either the execute command or a scoreboard shortcut command
        if self.current_token.matches(SelectorTypedToken.SELECTOR_VARIABLE):
            selector_begin_node = self.selector_begin_cmd()
            command_segment_nodes.append(selector_begin_node)

        if not self.current_token.matches(WhitespaceSimpleToken.NEWLINE):
            # guaranteed to be a scoreboard shortcut command with a selector
            if self.current_token.matches(SelectorTypedToken.SELECTOR_VARIABLE):
                scoreboard_cmd_node = self.scoreboard_cmd()
                command_segment_nodes.append(scoreboard_cmd_node)

            # string can be a target no matter the context
            elif self.current_token.matches(TypedToken.STRING):
                if self.current_token.value not in Parser.config_data.commands:
                    # if the string is not a command specified in the config data, guaranteed scoreboard shortcut
                    command_node = self.scoreboard_cmd()
                else:
                    command_node = self.simple_cmd()
                command_segment_nodes.append(command_node)

            else:
                self.error("Expected a newline, selector or start of a simple command")

        command_node = CommandNode(command_segment_nodes)
        return command_node

    def selector_begin_cmd(self):
        """
        Intermediary function to determine whether a selector at the beginning
        of a command is part of a scoreboard shortcut or execute shortcut

        It is an execute shortcut if the second token is a selector, coordinate, part of the ExecuteSimpleToken class or a colon
        It is (assumed to be) a scoreboard shortcut otherwise

        Returns:
            ExecuteCmdNode: if the selector is part of an execute shortcut
            ScoreboardCmdNode: if the selector is part of a scoreboard shortcut
        """
        selector_node = self.selector()
        # assumes scoreboard objectives will not be the same as the execute shortcut simple tokens
        if (self.current_token.value in TokenValues.get(ExecuteSimpleToken) or is_coord_token(self.current_token) or 
                self.current_token.matches_any_of(SelectorTypedToken.SELECTOR_VARIABLE, SimpleToken.COLON)):
            return self.execute_cmd(begin_selector=selector_node)

        else:
            return self.scoreboard_cmd(begin_target=selector_node)

    def execute_cmd(self, begin_selector=None, objective=None):
        pass

    def scoreboard_cmd(self, begin_target=None):
        pass

    def simple_cmd(self):
        """
        Note that a simple command can turn into a scoreboard command
        because "tp" can be a fake player name being set to a scoreboard value
        """
        pass

    def selector(self):
        """
        """
        pass
    
    def __repr__(self):
        return "Parser[iterator={}, current_token={}]".format(repr(self.iterator), repr(self.current_token))

if __name__ == "__main__":
    from lexer import Lexer

    logging_setup.format_file_name("test_lexer.txt")

    with open("test_lexer.txt") as file:
        text = file.read()
    lexer = Lexer(text)

    parser = Parser(lexer, r"C:\Users\Austin-zs\AppData\Roaming\.minecraft\saves\Snapshot 17w18b\data\functions\ego\event_name")

    try:
        parser.parse()
    except Exception as e:
        logging.exception(e)

    # mcfunctions = parser.mcfunctions
    # for mcfunction in mcfunctions:
    #     logging.debug("")
    #     logging.debug(str(mcfunction))
    #     for command in mcfunction.commands:
    #         logging.debug(repr(command))
