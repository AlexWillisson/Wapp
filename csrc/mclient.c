#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <memory.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <arpa/inet.h>

void
usage (void)
{
	fprintf (stderr, "usage: mclient msg\n");
	exit (1);
}

#define MPORT 20151

int
main (int argc, char **argv)
{
	int c;
	int sock;
	struct sockaddr_in addr;
	char *msg;

	while ((c = getopt (argc, argv, "")) != EOF) {
		switch (c) {
		default:
			usage ();
		}
	}

	if (optind >= argc)
		usage ();

	msg = argv[optind++];

	if (optind != argc)
		usage ();

	sock = socket (AF_INET, SOCK_DGRAM, 0);
	memset (&addr, 0, sizeof addr);
	addr.sin_family = AF_INET;
	inet_aton ("224.0.0.1", &addr.sin_addr);
	addr.sin_port = htons (MPORT);
	
	sendto (sock, msg, strlen (msg), 0, 
		(struct sockaddr *)&addr, sizeof addr);

	return (0);
}
