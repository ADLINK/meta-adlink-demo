#include<stdint.h>
#include<string.h>
#include<fcntl.h>
#include<unistd.h>
#include<stdio.h>
#include<stdlib.h>
#include<linux/input.h>
#include<unistd.h>

int main(int argc, char *argv[])
{
	int fd, version, duration, ret;
	struct input_event event;

	if (argc == 1) {
		printf("Usage: beep </dev/input/eventX> [freq] [duration]");
		return 1;
	}
	if ((fd = open(argv[1], O_RDWR)) < 0) {
		perror("beep test");
		return 1;
	}
	if (argc == 2) {
		event.value = 1000;
		duration = 3;
	} else if (argc == 3) {
		event.value = atoi(argv[2]);
		duration = 3;
	} else if (argc == 4) {
		event.value = atoi(argv[2]);
		duration = atoi(argv[3]);
	} else {
		printf("Usage: beep </dev/input/eventX> [freq] [duration]");
		return 1;
	}
	event.type = EV_SND;
	event.code = SND_TONE;

	printf("Open %s with SND_TONE %d for %d seconds\n", argv[1], event.value, duration);
	ret = write(fd, &event, sizeof(struct input_event));
	printf("ret = %d\n", ret);
	sleep(duration);
	event.value = 0;
	ret = write(fd, &event, sizeof(struct input_event));
	printf("ret = %d\n", ret);

	close(fd);

	return ret;
}

