LOCAL_PATH := $(call my-dir)

#
# Build webserver executable
#

include $(CLEAR_VARS)

LOCAL_CFLAGS	       := -D MG_ENABLE_IPV6 -DMG_TLS=MG_TLS_OPENSSL -std=c99 -O2 -W -Wall -lcrypto -lssl -pthread -pipe $(COPT)
LOCAL_MODULE           := webserver
LOCAL_SRC_FILES        := webserver.c mongoose/mongoose.c
LOCAL_SHARED_LIBRARIES := ssl crypto
LOCAL_LDLIBS           := -llog

include $(BUILD_EXECUTABLE)

#
# Import OpenSSL (ssl and crypto) libraries from prefab
#
$(call import-module,prefab/openssl)
