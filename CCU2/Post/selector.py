"""
A module for getting the selector shortcut from a selector token

This contains its own miniature lexer and interpreter to make
error reporting and changing easier
"""

import logging

from Common.lexicalToken import Token
from Post.configData import options
from Post.constants import SELECTOR_TYPES, SELECTOR_VARIABLES

SELECTOR_TYPE = "selector type"
SELECTOR_VAR = "selector variable"
STRING = "string"
INTEGER = "integer"

TYPE, VALUE = 0, 1
OPEN_BRACKET = ("open bracket", "[")
CLOSE_BRACKET = ("close bracket", "]")
EQUALS = ("equals", "=")
RANGE = ("range", "..")
COMMA = ("comma", ",")
SIMPLE_TOKENS = (OPEN_BRACKET, CLOSE_BRACKET, EQUALS, RANGE, COMMA)

END = ("end", "end")

selectorVarShort = {
    "limit": "c",
    "gamemode": "m",
    "dist": "distance",
    "lvl": "level",
    "x_rot": "x_rotation",
    "y_rot": "y_rotation",
}

selectorRangeLookup = {
    "distance": ("r", "rm"),
    "level": ("l", "lm"),
    "x_rotation": ("rx", "rxm"),
    "y_rotation": ("ry", "rym"),
}


class Lexer:
    """
    Miniature lexer for getting tokens for a selector

    Converts them into simple strings, separated into:
        '[' as open bracket
        ']' as close bracket
        '..' as range
        '=' as equal

        '@e', '@s', '@r', '@p', '@a' as type
        'lvl', 'c', 'objective', ... as strings
        '-5', '4', '0', ... as integers
    """

    def __init__(self, selector, posDisp):
        self.selector = selector
        self.pos = 0
        self.posDisp = list(posDisp)
        self.reachedEnd = False

    def getCurrentChars(self, length=1):
        """
        Args:
            length (int, optional) number of characters from the current position

        Returns:
            int: current characters from the current position given the length
        """
        return self.selector[self.pos: self.pos + length]

    def getPosRepr(self):
        line, column = self.posDisp
        return "Line {0} column {1}: ".format(line, column)

    def getTokenPos(self):
        """
        Note that this should only be used for creating tokens

        Returns:
            tuple (int, int): position of the token
        """
        return tuple(self.posDisp)

    def advance(self, increment=1):
        while increment > 0:
            self.pos += 1
            self.posDisp[1] += 1

            if self.pos > len(self.selector) - 1:
                self.reachedEnd = True
                break

            increment -= 1

    def getTokenList(self):
        tokenList = []
        while not self.reachedEnd:
            token = self.getNextToken()
            tokenList.append(token)

        return tokenList

    def getInteger(self):
        result = ""
        tokenPos = self.getTokenPos()

        if self.getCurrentChars() == "-":
            result = "-"
            self.advance()
        
        while not self.reachedEnd and self.getCurrentChars().isdigit():
            result += self.getCurrentChars()
            self.advance()

        return Token(tokenPos, INTEGER, int(result))

    def getString(self):
        result = ""
        tokenPos = self.getTokenPos()

        while not self.reachedEnd and (self.getCurrentChars().isalpha() or self.getCurrentChars() == "_"):
            result += self.getCurrentChars()
            self.advance()

        for selectorVar in options[SELECTOR_VARIABLES]:
            if result == selectorVar:
                return Token(tokenPos, SELECTOR_VAR, selectorVar)

        return Token(tokenPos, STRING, result)
    
    def getNextToken(self):
        if self.reachedEnd:
            return Token(self.getTokenPos(), END)

        for token in SIMPLE_TOKENS:
            if self.getCurrentChars(len(token[VALUE])) == token[VALUE]:
                tokenPos = self.getTokenPos()
                self.advance(len(token[VALUE]))
                return Token(tokenPos, token)

        if self.getCurrentChars(2) in options[SELECTOR_TYPES]:
            tokenPos = self.getTokenPos()
            selectorType = self.getCurrentChars(2)
            self.advance(2)
            return Token(tokenPos, SELECTOR_TYPE, selectorType)

        if self.getCurrentChars().isdigit() or self.getCurrentChars() == "-":
            return self.getInteger()

        if self.getCurrentChars().isalpha() or self.getCurrentChars() == "_":
            return self.getString()

        raise SyntaxError("Invalid character at {}".format(self.getPosRepr()))



class Interpreter:
    """

    Attributes:
        selectorStr (str): The full selector shortcut string conversion
        lexer (Lexer)
        currentToken (Token)
    """

    def __init__(self, lexer):
        self.selectorStr = ""
        self.lexer = lexer
        self.currentToken = None
        self.advance()

    def advance(self):
        self.currentToken = self.lexer.getNextToken()
        logging.debug("Advanced to selector {}".format(self.currentToken))

    def eat(self, *types, addToStr=False):
        if self.currentToken.matchesOne(*types):
            if addToStr:
                self.selectorStr += self.currentToken.value

            eatenToken = self.currentToken
            self.advance()

            return eatenToken
        else:
            self.error("Invalid type while eating")

    def error(self, message, token=None):
        if token is None:
            logging.error("Error during selector shortcut creation at {0}: {1}".format(self.currentToken, message))
        else:
            logging.error("Error during selector shortcut creation at {0}: {1}".format(token, message))
        raise SyntaxError

    def selector(self):
        """
        selector ::= SELECTOR_TYPE & ("[" & selectorArgs & "]")?
        """
        # expects a selector type always at first
        self.eat(SELECTOR_TYPE, addToStr=True)

        if self.currentToken.matches(OPEN_BRACKET):
            self.eat(OPEN_BRACKET, addToStr=True)
            self.selectorArgs()
            self.eat(CLOSE_BRACKET, addToStr=True)

        if not self.currentToken.matches(END):
            self.error("Expected end of selector")

        return self.selectorStr

    def selectorArgs(self):
        """
        selectorArgs ::= (singleArg)? | (singleArg & ("," & singleArg))?
        """
        if not self.currentToken.matches(CLOSE_BRACKET):
            self.singleArg()

        while not self.currentToken.matches(CLOSE_BRACKET):
            self.eat(COMMA, addToStr=True)
            self.singleArg()

    def singleArg(self):
        """
        singleArg ::= STRING & ("=" & [range, STRING])?
        """
        selectorVar = self.eat(STRING, SELECTOR_VAR)

        if self.currentToken.matches(EQUALS):
            self.eat(EQUALS)
            if self.currentToken.matches(STRING):

                # gets the shortcut version if it exists
                selectorVarStr = selectorVarShort.get(selectorVar.value, selectorVar.value)

                if selectorVarStr not in options[SELECTOR_VARIABLES]:
                    self.error("A string selector argument value cannot exist without a default selector variable")
                else:
                    stringValue = self.eat(STRING).value
                    self.selectorStr += "{0}={1}".format(selectorVarStr, stringValue)
            else:
                self.range(selectorVar)

        else:
            self.selectorStr += "tag={}".format(selectorVar.value)

    def range(self, selectorVar):
        """
        range ::= [INTEGER, (INTEGER & ".."), (".." & INTEGER), (INTEGER & ".." & INTEGER)]

        Args:
            selectorVar (Token)
        """
        # min within "min..max"
        minToken = None
        maxToken = None

        # whether the ".." actually exists or not
        sameVal = False

        if self.currentToken.matches(INTEGER):
            minToken = self.eat(INTEGER)
        if not self.currentToken.matches(RANGE):
            sameVal = True
        else:
            self.eat(RANGE)
            if self.currentToken.matches(INTEGER):
                maxToken = self.eat(INTEGER)

        # if both min and max are none, raises error since that shouldn't happen
        if (minToken, maxToken).count(None) == 2:
            self.error("Range has no integers")

        self.useRange(selectorVar, minToken, maxToken, sameVal)

    def useRange(self, selectorVar, minToken, maxToken, sameVal):
        """
        Converts the range to a string

        Args:
            selectorVar (str)
            minToken (Token or None)
            maxToken (Token or None)
        """

        # holds all var=value
        argList = []

        # gets the shortcut version if it exists
        selectorVarStr = selectorVarShort.get(selectorVar.value, selectorVar.value)

        # if the selector var is part of the default selector
        # variables, it will use the min value
        if selectorVarStr in options[SELECTOR_VARIABLES]:
            if not sameVal:
                self.error("Default selector variables cannot have ranges")
            self.selectorStr += "{0}={1}".format(selectorVarStr, str(minToken.value))

        else:
            # checks whether the beginning and ending variables should be different
            # due to conversion back to 1.12
            begVar, endVar = selectorRangeLookup.get(selectorVarStr, ("score_{}_min".format(selectorVarStr), "score_{}".format(selectorVarStr)))
            if minToken is not None:
                argList.append(begVar + "=" + str(minToken.value))

            if maxToken is not None:
                argList.append(endVar + "=" + str(maxToken.value))

            self.selectorStr += ",".join(argList)

def getSelector(selectorToken):
    selector = selectorToken.value
    pos = selectorToken.pos
    lexer = Lexer(selector, pos)
    interpreter = Interpreter(lexer)
    return interpreter.selector()
