#!/usr/bin/env python

import os
import re
import pprint

# Python 2.x and 3.x compatability
try:
    from tkinter import *
    import tkinter.filedialog
    import tkinter.messagebox
except ImportError as ex:
    # Must be using Python 2.x, import and rename
    from Tkinter import *
    import tkFileDialog
    import tkMessageBox

    tkinter.filedialog = tkFileDialog
    del tkFileDialog
    tkinter.messagebox = tkMessageBox
    del tkMessageBox

sys.path.append(os.path.join('pymavlink','generator'))
from mavgen import *

class MavgenOptions:
    def __init__(self,language,protocol,output,error_limit):
        self.language = language
        self.wire_protocol = protocol
        self.output = output
        self.error_limit = error_limit;


if __name__ == '__main__':
# Generate headers
    opts = MavgenOptions('Java', '1.0', '../src/com/MAVLink/Messages', '200');
    args = ['./message_definitions/ardupilotmega.xml']
    mavgen(opts,args)
