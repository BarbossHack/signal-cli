FROM docker.io/debian:testing-slim

LABEL org.opencontainers.image.source=https://github.com/AsamK/signal-cli
LABEL org.opencontainers.image.description="signal-cli provides an unofficial commandline, dbus and JSON-RPC interface for the Signal messenger."
LABEL org.opencontainers.image.licenses=GPL-3.0-only

RUN useradd signal-cli --system
ADD client/target/release/signal-cli-client /usr/bin/signal-cli-client

USER signal-cli
ENTRYPOINT ["/usr/bin/signal-cli-client"]
