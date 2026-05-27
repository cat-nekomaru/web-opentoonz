# so simple: `.tlw2` offset address diagram

wip rn. thx.

<div align="center">
<img width="550" alt="Image" src="https://github.com/user-attachments/assets/2be5bd7b-82cc-4559-a5d9-1417b6e0224c" />
</div>  

.  

```
struct TLW2_Header {
                               // *** fixed 128byte ***
    char     magic[16];        // TLW2_JMP_FOOTER\0, TLW2_JMP_INDEX\0\0
    uint32_t format_version;   // 4B
    uint32_t flags;            // 4B
    uint64_t total_size;       // 8B
    uint64_t json_offset;      // 8B
    uint64_t json_size;        // 8B
    uint64_t checksum;         // xxHash64
    uint64_t creation_time;    // Unix timestamp
    char     comment[64];      // comment, UTF-8

                               // *** free 896byte ***
    uint8_t  reserved[1024 - 128];
};
```

```
struct TLW2_Footer {
                               // *** fixed 128byte ***
    char     magic[16];        // TLW2_DEV_202605\0, TLW2_REL_202701\0
    uint32_t format_version;   // 4B
    uint32_t flags;            // 4B
    uint64_t total_size;       // 8B
    uint64_t json_offset;      // 8B
    uint64_t json_size;        // 8B
    uint64_t checksum;         // xxHash64
    uint64_t creation_time;    // Unix timestamp
    char     comment[64];      // comment, UTF-8
};
```

🐾
---
