import signal
from threading import Timer

import pygogo

logger = pygogo.Gogo(high_level='error').logger


def main():
    logger.info("Starting application")

    def _run():
        logger.info("Keeping running")

    try:
        thread = Timer(interval=2, function=_run)
        thread.start()
        signal.pause()
    except KeyboardInterrupt:
        logger.info("Stopping application")


if __name__ == '__main__':
    main()
