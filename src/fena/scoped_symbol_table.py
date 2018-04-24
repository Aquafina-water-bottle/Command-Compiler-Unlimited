import os
import itertools

from token_types import TokenType
from lexical_token import Token

class ScopedSymbolTable:
    """
    Attributes:
        is_global (bool)
        scope_level (int)
        enclosing_scope (ScopedSymbolTable or None)

        function (McFunction or None)
        constobj (str or None)
        prefix (str or None)
        folders (tuple of strs)
        command_slices (tuple of strs)
    """

    def __init__(self, enclosing_scope=None):
        self.enclosing_scope = enclosing_scope

        if enclosing_scope is None:
            self.scope_level = 0
            self.is_global = True
            self._function = None
            self._constobj = None
            self._prefix = None
            self._folders = ()
            self._command_slices = ()

        else:
            assert isinstance(self.enclosing_scope, ScopedSymbolTable)
            self.is_global = False
            self.scope_level = enclosing_scope.scope_level + 1
            self._function = enclosing_scope._function
            self._constobj = enclosing_scope._constobj
            self._prefix = enclosing_scope._prefix
            self._folders = enclosing_scope._folders
            self._command_slices = enclosing_scope._command_slices

        self._update_command_slices()

    def add_folder(self, folder):
        assert isinstance(folder, str)
        self._folders += (folder,)

    def add_command_slice(self, command_slice):
        """
        Updates the command slice by adding the proper string to the tuple

        Args:
            command_slice (Token): Token with type COMMAND
        """
        valid_type = Token
        assert isinstance(command_slice, valid_type), "Expected {} but got {}".format(valid_type, type(command_slice))
        valid_token_type = TokenType.COMMAND
        assert command_slice.matches(valid_token_type), "Expected {} but got {}".format(valid_token_type, command_slice.type)
        assert command_slice.value[-1] == ":", "Expected ':' but got {}".format(command_slice.value[-1])

        self._command_slices += (command_slice.value[:-1],)
        self._update_command_slices()

    def _update_command_slices(self):
        if self._command_slices:
            # adds a leading whitespace so commands can be properly added to it
            self._command_slices_str = " ".join(self._command_slices) + " "
        else:
            self._command_slices_str = ""

    @property
    def function(self):
        return self._function

    @function.setter
    def function(self, function):
        """
        A function can only be set if there is not already one set
        """
        assert self.function is None, "The function can only be set if one has not already been set"
        self._function = function

    @property
    def constobj(self):
        return self._constobj

    @constobj.setter
    def constobj(self, constobj):
        """
        The constobj can only be set once in the global scope
        """
        assert self.is_global, "The constobj can only be set in the global context"
        assert self.constobj is None, "The constobj can only be set if the constobj has not been previously set"
        self._constobj = constobj

    @property
    def prefix(self):
        return self._prefix

    @prefix.setter
    def prefix(self, prefix):
        """
        A prefix can only be set if it is in the global scope
        """
        assert self.is_global, "The prefix can only be set in the global context"
        self._prefix = prefix

    @property
    def folders(self):
        """
        Returns:
            str: The string representation of the folder concatenation
                returns a 0 length string if folders is an empty list
        """
        if not self._folders:
            return None
        return os.path.join(*self._folders)

    @property
    def command_slices(self):
        """
        Concatenation of all strings in a tuple into one string

        Returns:
            str: The concatenation of all command slices
                This also adds an extra space at the end so commands can be properly added to it
        """
        return self._command_slices_str

    def __str__(self):
        return "[function={}, constobj={}, prefix={}, folders={}, command_slices={}]".format(
            self.function, repr(self.constobj), repr(self.prefix), repr(self.folders), self.command_slices
        )

    def __repr__(self):
        return "ScopedSymbolTable[scope_level={}, is_global={}, enclosing_scope={}, current_scope={}]".format(
            self.scope_level, self.is_global, self.enclosing_scope, self
        )