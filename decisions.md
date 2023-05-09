# Decisions

## Don't Use Live Data. Prefer SharedFlow
LiveData has the unfortunate requirement that it can only be updated on the main thread. This
creates a problem if we need to do any updates while blocking the main thread. Usually we don't want
to block the main thread, but there are some cases where we have to (for example onShutdown to
ensure we have completed all work needed to save before the shutdown).

Instead prefer SharedFlow which can send and receive updates on other threads.
