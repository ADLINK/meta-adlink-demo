#!/usr/bin/env python3

# Copyright (c) 2023 Adlink Corp, Inc. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
# 3. The names of the authors may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
# TECHNEXION, INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
#
# Author: Po Cheng <po.cheng@adlinktech.com>

# operation control:
# This script pack json parsed data into messagepack binary format
# and is store on an eeprom. The script can also read back the messagepacked
# binary data into json.

import json
import msgpack
import smbus2 as smbus
import argparse
import time
import sys

# I2C parameters
I2C_BUS = 1  # The I2C bus number
EEPROM_ADDRESS = 0x54  # Replace this with the actual EEPROM I2C address
EEPROM_SIZE = 1024 # size of the eeprom
EEPROM_WRITE_DELAY = 0.005 # write delay in seconds

# Write data to EEPROM
def write_eeprom_data(bus_number, address, data):
    with smbus.SMBus(bus_number) as bus:
        for i, byte in enumerate(data):
            bus.write_byte_data(address, i, byte)
            time.sleep(EEPROM_WRITE_DELAY)  # Wait between writes (adjust as needed)

# Read data from EEPROM
def read_eeprom_data(bus_number, address, length):
    data = []
    with smbus.SMBus(bus_number) as bus:
        for offset in range(0, length, smbus.smbus2.I2C_SMBUS_BLOCK_MAX):
            data += bus.read_i2c_block_data(address, offset, smbus.smbus2.I2C_SMBUS_BLOCK_MAX)
    return data

def json_to_msgpack(bus_number, address, length, config):
    if bus_number is not None and address is not None and length is not None:
        packed = msgpack.packb(config)
        if len(packed) < length:
            # write eeprom
            write_eeprom_data(bus_number, address, packed)
            print("{} bytes written to eeprom".format(len(packed)))
        else:
            print("Error: msgpacked config size too large for eeprom")
    else:
        print("Error: invalid parameters")

def msgpack_to_json(bus_number, address, length):
    # read eeprom
    if bus_number is not None and address is not None and length is not None:
        packed = bytearray(read_eeprom_data(bus_number, address, length))
        data = packed.split(b'\xa1-\xa1-')[0]
        data.extend(b'\xa1-\xa1-')
        config = msgpack.unpackb(data, raw=False)
        # NOTE: check for a1 2d a1 2d that indicate end of json. and remove it.
        config.pop("-")
        return config
    else:
        print("Error: invalid parameters")

def setup_parser():
    parser = argparse.ArgumentParser(description='msgpacker command argument parser')
    subparsers = parser.add_subparsers(dest='cmd', help='commands')
    ############################################################################
    # read commands
    # 'tgt_json', i2c_bus, eeprom_address, eeprom_size
    ############################################################################
    read_parser = subparsers.add_parser('read', help='read from eeprom')
    read_parser.add_argument('-b', '--bus', dest='i2c_bus', \
                              action='store', default=I2C_BUS, \
                              help='Specify i2c bus number of EEPROM device')
    read_parser.add_argument('-a', '--eeprom-address', dest='eeprom_address', \
                              action='store', default=EEPROM_ADDRESS, \
                              help='Specify eeprom address on i2c bus')
    read_parser.add_argument('-n', '--eeprom-size', dest='eeprom_size', \
                              action='store', default=EEPROM_SIZE, \
                              help='Specify eeprom size in bytes')
    read_parser.add_argument('-j', '--target-json', dest='tgt_json', \
                              action='store', metavar='FILENAME', \
                              help='Specify target json filename to write to')
    ############################################################################
    # write commands
    # 'src_json', i2c_bus, eeprom_address, eeprom_size
    ############################################################################
    write_parser = subparsers.add_parser('write', help='write to eeprom')
    write_parser.add_argument('-b', '--bus', dest='i2c_bus', \
                              action='store', default=I2C_BUS, \
                              help='Specify i2c bus number of EEPROM device')
    write_parser.add_argument('-a', '--eeprom-address', dest='eeprom_address', \
                              action='store', default=EEPROM_ADDRESS, \
                              help='Specify eeprom address on i2c bus')
    write_parser.add_argument('-n', '--eeprom-size', dest='eeprom_size', \
                              action='store', default=EEPROM_SIZE, \
                              help='Specify eeprom size in bytes')
    write_parser.add_argument('-j', '--src-json', dest='src_json', \
                              action='store', metavar='FILENAME', \
                              help='Specify src json filename to read from')
    ############################################################################
    # get commands
    # i2c_bus, eeprom_address, eeprom_size, 'key name'
    ############################################################################
    get_parser = subparsers.add_parser('get', help='get key/value from eeprom')
    get_parser.add_argument('-b', '--bus', dest='i2c_bus', \
                              action='store', default=I2C_BUS, \
                              help='Specify i2c bus number of EEPROM device')
    get_parser.add_argument('-a', '--eeprom-address', dest='eeprom_address', \
                              action='store', default=EEPROM_ADDRESS, \
                              help='Specify eeprom address on i2c bus')
    get_parser.add_argument('-n', '--eeprom-size', dest='eeprom_size', \
                              action='store', default=EEPROM_SIZE, \
                              help='Specify eeprom size in bytes')
    get_parser.add_argument('-k', '--key-name', dest='key_name', \
                              action='store', help='Specify key name to read from eeprom')
    ############################################################################
    # set commands
    # i2c_bus, eeprom_address, eeprom_size, 'key name', 'value'
    ############################################################################
    set_parser = subparsers.add_parser('set', help='set key with value to eeprom')
    set_parser.add_argument('-b', '--bus', dest='i2c_bus', \
                              action='store', default=I2C_BUS, \
                              help='Specify i2c bus number of EEPROM device')
    set_parser.add_argument('-a', '--eeprom-address', dest='eeprom_address', \
                              action='store', default=EEPROM_ADDRESS, \
                              help='Specify eeprom address on i2c bus')
    set_parser.add_argument('-n', '--eeprom-size', dest='eeprom_size', \
                              action='store', default=EEPROM_SIZE, \
                              help='Specify eeprom size in bytes')
    set_parser.add_argument('-k', '--key-name', dest='key_name', \
                              action='store', help='Specify key name to set to eeprom')
    set_parser.add_argument('-v', '--value', dest='key_value', \
                              action='store', help='Specify value for the key')
    ############################################################################
    # remove commands
    # i2c_bus, eeprom_address, eeprom_size, 'key name'
    ############################################################################
    get_parser = subparsers.add_parser('remove', help='remove key/value from eeprom')
    get_parser.add_argument('-b', '--bus', dest='i2c_bus', \
                              action='store', default=I2C_BUS, \
                              help='Specify i2c bus number of EEPROM device')
    get_parser.add_argument('-a', '--eeprom-address', dest='eeprom_address', \
                              action='store', default=EEPROM_ADDRESS, \
                              help='Specify eeprom address on i2c bus')
    get_parser.add_argument('-n', '--eeprom-size', dest='eeprom_size', \
                              action='store', default=EEPROM_SIZE, \
                              help='Specify eeprom size in bytes')
    get_parser.add_argument('-k', '--key-name', dest='key_name', \
                              action='store', help='Specify key name to read from eeprom')
    return parser

def main():
    # by default, arguments taken from sys.argv[1:] and convert to dict using vars() on NameSpace
    args = vars(setup_parser().parse_args())
    if 'cmd' in args and args['cmd'] == 'read':
        # Write unpacked data to JSON file
        if args['tgt_json'] is not None:
            with open(args['tgt_json'], 'w') as js:
                json.dump(msgpack_to_json(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size'])), js, indent=4)
        else:
            json.dump(msgpack_to_json(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size'])), sys.stdout, indent=4)
    elif 'cmd' in args and args['cmd'] == 'write':
        # Write packed data to MessagePack file
        with open(args['src_json'], 'r') as js:
            config = json.load(js)
            # NOTE: to ensure we have a1 2d a1 2d to indicate the end of data.
            config.update({"-": "-"})
        json_to_msgpack(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']), config)
    elif 'cmd' in args and args['cmd'] == 'get':
        jsdata = msgpack_to_json(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']))
        if args['key_name'] in jsdata.keys():
            print (f"{args['key_name']}: {jsdata[args['key_name']]}")
        else:
            print (f"{args['key_name']} not found.")
    elif 'cmd' in args and args['cmd'] == 'set':
        jsdata = msgpack_to_json(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']))
        if jsdata != None:
            if args['key_name'] in jsdata.keys():
                # modify
                try:
                    if (int(args['key_value']) != 0):
                        jsdata[args['key_name']] = int(args['key_value'])
                    elif isinstance(args['key_value'], String):
                        jsdata[args['key_name']] = f"{args['key_value']}"
                except ValueError:
                    jsdata[args['key_name']] = f"{args['key_value']}"
            else:
                # insert
                try:
                    if (int(args['key_value']) != 0):
                        jsdata.update({args['key_name']: int(args['key_value'])})
                    elif isinstance(args['key_value'], String):
                        jsdata.update({args['key_name']: f"{args['key_value']}"})
                except ValueError:
                    jsdata.update({args['key_name']: f"{args['key_value']}"})
            # NOTE: to ensure we have a1 2d a1 2d to indicate the end of data.
            jsdata.update({"-":"-"})
            json_to_msgpack(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']), jsdata)
        else:
            # noothing written to eeprom
            jsdata = {}
            try:
                if (int(args['key_value']) != 0):
                    jsdata.update({args['key_name']: int(args['key_value'])})
                elif isinstance(args['key_value'], String):
                    jsdata.update({args['key_name']: f"{args['key_value']}"})
            except ValueError:
                jsdata.update({args['key_name']: f"{args['key_value']}"})
            # NOTE: to ensure we have a1 2d a1 2d to indicate the end of data.
            jsdata.update({"-":"-"})
            json_to_msgpack(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']), jsdata)
    elif 'cmd' in args and args['cmd'] == 'remove':
        jsdata = msgpack_to_json(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']))
        if args['key_name'] in jsdata.keys():
            del jsdata[args['key_name']]
            # NOTE: to ensure we have a1 2d a1 2d to indicate the end of data.
            jsdata.update({"-": "-"})
            json_to_msgpack(int(args['i2c_bus']), int(args['eeprom_address'], 16), int(args['eeprom_size']), jsdata)
        else:
            print (f"{args['key_name']} not found.")



if __name__ == "__main__":
    main()
