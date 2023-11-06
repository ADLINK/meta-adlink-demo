import gpiod
import sys
import os

def handle_event(event, dout, action):
    try:
        if event.type == gpiod.LineEvent.RISING_EDGE:
            evstr = 'RISING EDGE'
            if dout:
                dout.set_values([1])
        elif event.type == gpiod.LineEvent.FALLING_EDGE:
            evstr = 'FALLING EDGE'
            if dout:
                dout.set_values([0])
        else:
            raise TypeError('Invalid event type')
        if action == "print":
            print('event: {} offset: {} timestamp: [{}.{}]'.format(evstr, event.source.offset(), event.sec, event.nsec))
        else:
            os.system(f"{action} {event.type}")
    except Exception as e:
        print(e)



def setup_parser():
    parser = argparse.ArgumentParser(description='gpiomon-action command argument parser')
    # GPIO_CHIP = "/dev/gpiochip0"
    parser.add_argument('-c', '--chip', dest='chip', \
                              action='store', default="/dev/gpiochip0", \
                              help='Specify gpiochip number')
    # INPUT = 10 # USB1_OTG_ID
    parser.add_argument('-i', '--input', dest='input', \
                              action='store', default=10, \
                              help='Specify input gpio pin')
    # OUTPUT = 1 # USB_MUX_SEL
    parser.add_argument('-o', '--output', dest='output', \
                              action='store', default=1, \
                              help='Specify output gpio pin')
    parser.add_argument('-a', '--action', dest='action', \
                              action='store', default="print" \
                              help='Specify action for the input gpio detection')
    return parser



def main():

    # by default, arguments taken from sys.argv[1:] and convert to dict using vars() on NameSpace
    args = vars(setup_parser().parse_args())

    if len(sys.argv) < 3:
        raise TypeError('usage: gpiomon-action.py -c/--chip <gpiochip> -i/--input <input pin> -o/--output <output pin> -a/--action <command>')

    with gpiod.Chip(args['chip']) as chip:
        din = chip.get_lines(int(args['input'])
        din.request(consumer=sys.argv[0], type=gpiod.LINE_REQ_EV_BOTH_EDGES)
        dout = chip.get_lines(int(args['output'])
        try:
            while True:
                ev_lines = din.event_wait(sec=1)
                if ev_lines:
                    for line in ev_lines:
                        event = line.event_read()
                        handle_event(event, dout, args['action'])
        except KeyboardInterrupt:
            sys.exit(130)

if __name__ == "__main__":
    main()
