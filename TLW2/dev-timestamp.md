# memo: ApfsTime for Android

wip. from Jun 1. thanks.  

.  
.  

```cpp

struct TimerState {             // #### ApfsTime for Android ####
                                //
    int64_t masterApfsNanos;    // ApfsTime, nanoseconds since 1970 UTC
    int64_t localBaseNanos;     // monotonic time, based on android API
    int64_t apfsOffsetFromLocal; // ApfsTime = this + localBaseNanos
    int64_t counterValue10k;    // 64-bit signed, preroll OK, 10,000/sec
    uint64_t hostId;            // 8B
    uint8_t flag;               // 1B
};

```

.  

🐾
---
