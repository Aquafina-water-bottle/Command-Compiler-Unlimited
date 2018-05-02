"""
Singleton class that contains:
    objectives dict
    tags dict
    teams dict
    functions dict

    constobj
    prefix
"""

import logging
import os.path
from collections import deque

from mcfunction import McFunction
from lexical_token import Token

class InFileConfig:
    """
    Holds branching simple commands using a builder model
    Args:
        prefix (str): Any prefix to objectives, teams or tags
        constobj (str): The objective used for scoreboard players operation with a constant value
        objectives (dict): Holds all properly named objectives and shortcut objectives
        tags (dict): Holds all properly named tags and shortcut objectives
            - Note that tags are also defined in nbt data
        teams (dict): Holds all properly named teams and shortcut objectives
            - Note that a team can also be defined in nbt data
                - objectives, tags and teams all map to _name -> prefix.name and prefix.name -> prefix.name
                - the reason for prefix.name -> prefix.name is for containment tests and
                  for ensuring all objs/tags/teams can be mapped if contained
        functions (dict): All function shortcuts present in the file
            - Maps any version of the function shortcut to the function path
    """

    def __init__(self):
        self.objectives = {}
        self.tags = {}
        self.teams = {}
        self.functions = {}
        self.function_conflicts = set()
        self._prefix = None
        self._constobj = None
    
    def __new__(cls):
        """
        Ensures they are the same class
        """
        if not hasattr(cls, '_in_file_config_data'):
            cls._in_file_config_data = super().__new__(cls)
        return cls._in_file_config_data

    @property
    def prefix(self):
        return self._prefix

    @prefix.setter
    def prefix(self, prefix):
        """
        Does not allow the constobj to be set multiple times

        Args:
            prefix (Token)
        """
        if self._prefix is not None:
            raise SyntaxError("{}: Cannot set a prefix twice".format(prefix))

        assert isinstance(prefix, Token)
        assert isinstance(prefix.value, str)
        self._prefix = prefix.value

    @property
    def constobj(self):
        return self._constobj

    @constobj.setter
    def constobj(self, constobj):
        """
        Does not allow the constobj to be set multiple times

        Args:
            prefix (Token)
        """
        if self._constobj is not None:
            raise SyntaxError("{}: Cannot set a constobj twice".format(constobj))

        assert isinstance(constobj, Token)
        assert isinstance(constobj.value, str)
        self._constobj = constobj.value

    def add_function(self, mcfunction):
        """
        Gets the function paths all the way until "functions"

        Args:
            mcfunction (McFunction)
        """
        assert isinstance(mcfunction, McFunction)
        full_path = mcfunction.full_path

        # strips away ".mcfunction"
        path_without_ext, extension = os.path.splitext(full_path)
        assert extension == ".mcfunction"

        # gets the list of all directories including base file without extension
        path_list = os.path.normpath(path_without_ext).split(os.sep)

        # gets all directories of the shortcut including the function name
        # path_list should contain "functions" by the end, or else it will be an empty list
        directories = deque()
        while path_list and path_list[-1] != "functions":
            directories.appendleft(path_list.pop())

        if not path_list:
            raise SyntaxError("Path {} must contain a functions/ folder".format(full_path))

        if len(directories) <= 1:
            raise SyntaxError("Path {} must have a folder inside the functions/ folder".format(full_path))

        shortcuts, mcfunction_path = self._get_all_shortcuts(directories)
        self.functions.update(dict.fromkeys(shortcuts, mcfunction_path))

    def _get_all_shortcuts(self, directories):
        """
        Args:
            directories (list of strs): All directories up to but excluding the function folder

        Returns:
            list: All possible shortcuts to the mcfunction path
            str: The mcfunction path as specified by minecraft

        Examples:
            >>> i = InFileConfig()
            >>> directories = deque(["ego", "floo_network", "init"])
            >>> i._get_all_shortcuts(directories)
            (['ego:floo_network/init', 'floo_network/init', 'init'], 'ego:floo_network/init')
        """
        # gets the mcfunction path
        mcfunction_path = directories.popleft() + ":"
        mcfunction_path += "/".join(directories)

        # shortcuts also has the mcfunction path to map to itself to pass the FunctionBuilder containment test
        shortcuts = []
        shortcuts.append(mcfunction_path)

        # gets all shortcuts to the full name
        while directories:
            shortcut = "/".join(directories)
            shortcuts.append(shortcut)
            directories.popleft()

        return shortcuts, mcfunction_path

    def finalize(self):
        # default for prefix is "fena"
        if self.prefix is None:
            self.prefix = "fena"
            logging.warning("Using the default prefix of {}".format(self.prefix))
        
        # default for constobj is "g.number"
        if self.constobj is None:
            self.constobj = "g.number"
            logging.warning("Using the default constobj of {}".format(self.constobj))


if __name__ == "__main__":
    # mcfunction = McFunction(r"C:\Users\Austin-zs\Documents\Austin\powder game code\Programming\CCU\functions\ego\floo_network\init.mcfunction")
    # in_file_config = InFileConfig()

    # in_file_config.add_function(mcfunction)
    # print(in_file_config.functions)
    import doctest
    doctest.testmod()
    