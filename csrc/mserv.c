#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <memory.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <sys/socket.h>

void
usage (void)
{
	fprintf (stderr, "usage: mserv\n");
	exit (1);
}

#define MPORT 20151

int
main (int argc, char **argv)
{
	int c;
	int sock;
	struct sockaddr_in addr, raddr;
	int ival;
	fd_set rset;
	socklen_t raddrlen;
	int n;
	char rpkt[10000];

	while ((c = getopt (argc, argv, "")) != EOF) {
		switch (c) {
		default:
			usage ();
		}
	}

	if (optind != argc)
		usage ();

	sock = socket (AF_INET, SOCK_DGRAM, 0);
	memset (&addr, 0, sizeof addr);
	addr.sin_family = AF_INET;
	addr.sin_port = htons (MPORT);
	if (bind (sock, (struct sockaddr *)&addr, sizeof addr) < 0) {
		fprintf (stderr, "can't bind to port %d\n", MPORT);
		exit (1);
	}
	fcntl (sock, F_SETFL, O_NONBLOCK);

	ival = 1;
	if (setsockopt (sock, SOL_SOCKET, SO_REUSEADDR,
			&ival, sizeof ival) < 0) {
		perror ("SO_REUSEADDR");
		exit (1);
	}

	printf ("listening on %d\n", MPORT);

	while (1) {
		FD_ZERO (&rset);
		FD_SET (sock, &rset);

		if (select (sock + 1, &rset, NULL, NULL, NULL) < 0) {
			perror ("select error");
			exit (1);
		}

		raddrlen = sizeof raddr;
		n = recvfrom (sock, rpkt, sizeof rpkt - 1, 0,
			      (struct sockaddr *)&raddr, &raddrlen);

		if (n < 0) {
			perror ("recvfrom error");
			exit (1);
		}

		rpkt[n] = 0;
		printf ("rcv: %s\n", rpkt);
	}

	return (0);
}
