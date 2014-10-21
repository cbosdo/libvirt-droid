LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := virt-prebuilt
LOCAL_SRC_FILES := libvirt.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := jnidispatch-prebuilt
LOCAL_SRC_FILES := libjnidispatch.so

include $(PREBUILT_SHARED_LIBRARY)
