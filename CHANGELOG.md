# Changelog

### 1.0.1 (Summary 2020)

Patch:
- Added logic to Connect.onCreate() to immediately finish if it detects that Android has restarted the process.  This is because the static EventListener reference in Connect no longer exists and must be supplied by the parent activity.

### 1.0.2 (August 19, 2020)

Patch:
- Updated Connect.onCreate() to first call super.onCreate() before doing the EventListener / process restart check (see last change).  Turns out finishing an activity before calling super.onCreate() causes an exception.