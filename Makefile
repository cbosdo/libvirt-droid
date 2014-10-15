.DEFAULT_GOAL:=all

# These values match android NDK r10b, may be good to make that configurable
TARGET_TOOLCHAIN=arm-linux-androideabi
VERSION=4.9
PLATFORM=android-L

BUILDDIR=/tmp/ndk-build


PATH:=$(BUILDDIR)/bin:$(BUILDDIR)/$(TARGET_TOOLCHAIN)/bin:$(PATH)

# Disable shared libs for the dependencies to get a single libvirt.so containing it all
CONFIGURE_FLAGS=--host=$(TARGET_TOOLCHAIN) --prefix $(BUILDDIR)

ifeq ($(strip $(NDK_HOME)),)
ndk.setup:
	@echo "Please set NDK_HOME to the extracted Android NDK path"
	exit 1
else
ndk.setup:
	$(NDK_HOME)/build/tools/make-standalone-toolchain.sh \
		--toolchain=$(TARGET_TOOLCHAIN)-$(VERSION) \
		--platform=$(PLATFORM) --install-dir=$(BUILDDIR)
	touch $@
endif

ndk.clean:
	rm -rf $(BUILDDIR)
	rm -rf ndk.setup

# Rules to build portablexdr
XDR=portablexdr-4.9.1

$(XDR).tar.gz:
	wget http://people.redhat.com/~rjones/portablexdr/files/$@

$(XDR).unpacked: $(XDR).tar.gz
	tar xzf $<
	touch $@

$(XDR).patched: $(XDR).unpacked portablexdr-headers-fix.patch
	cd $(XDR) && patch -p1 <../portablexdr-headers-fix.patch
	touch $@

$(XDR).built: $(XDR).patched
	cd $(XDR) && \
	autoreconf -fi && \
	./configure $(CONFIGURE_FLAGS) && \
	make && \
	make install
	touch $@

$(XDR).clean:
	rm -f $(XDR).built
	rm -f $(XDR).patched
	rm -f $(XDR).unpacked
	rm -rf $(XDR)
	

# Rules to build libxml2

LIBXML2=libxml2-2.9.1

$(LIBXML2).tar.gz:
	wget ftp://xmlsoft.org/libxml2/$@

$(LIBXML2).unpacked: $(LIBXML2).tar.gz
	tar xzf $<
	touch $@

$(LIBXML2).patched: $(LIBXML2).unpacked libxml-disable-tests.patch
	cd $(LIBXML2) && patch -p1 <../libxml-disable-tests.patch
	touch $@

$(LIBXML2).built: $(LIBXML2).patched
	cd $(LIBXML2) && \
	autoreconf -fi && \
	./configure $(CONFIGURE_FLAGS) --without-lzma --without-python && \
	make && \
	make install
	touch $@

$(LIBXML2).clean:
	rm -f $(LIBXML2).built
	rm -f $(LIBXML2).patched
	rm -f $(LIBXML2).unpacked
	rm -rf $(LIBXML2)

# Rules to build libgpg-error
GPGERR=libgpg-error-1.16

$(GPGERR).tar.bz2:
	wget ftp://ftp.gnupg.org/gcrypt/libgpg-error/$@

$(GPGERR).unpacked: $(GPGERR).tar.bz2
	tar xjf $<
	touch $@

$(GPGERR).built: $(GPGERR).unpacked
	cd $(GPGERR) && \
	./configure $(CONFIGURE_FLAGS) && \
	make && \
	make install
	touch $@

$(GPGERR).clean:
	rm -f $(GPGERR).built
	rm -f $(GPGERR).patched
	rm -f $(GPGERR).unpacked
	rm -rf $(GPGERR)

# Rules to build libgcrypt
GCRYPT=libgcrypt-1.6.2

$(GCRYPT).tar.bz2:
	wget ftp://ftp.gnupg.org/gcrypt/libgcrypt/$@

$(GCRYPT).unpacked: $(GCRYPT).tar.bz2
	tar xjf $<
	touch $@

$(GCRYPT).built: $(GPGERR).built $(GCRYPT).unpacked
	cd $(GCRYPT) && \
	./configure $(CONFIGURE_FLAGS) && \
	make && \
	make install
	touch $@

$(GCRYPT).clean:
	rm -f $(GCRYPT).built
	rm -f $(GCRYPT).patched
	rm -f $(GCRYPT).unpacked
	rm -rf $(GCRYPT)


# Rules to build libssh

LIBSSH=libssh2-1.4.3

$(LIBSSH).tar.gz:
	wget http://www.libssh2.org/download/$@

$(LIBSSH).unpacked: $(LIBSSH).tar.gz
	tar xzf $<
	touch $@

$(LIBSSH).built: $(GCRYPT).built $(LIBSSH).unpacked
	cd $(LIBSSH) && \
	./configure $(CONFIGURE_FLAGS) && \
	make && \
	make install
	touch $@

$(LIBSSH).clean:
	rm -f $(LIBSSH).built
	rm -f $(LIBSSH).patched
	rm -f $(LIBSSH).unpacked
	rm -rf $(LIBSSH)


# Rules to build libnl
NL=libnl-3.2.25
$(NL).tar.gz:
	wget http://www.infradead.org/~tgr/libnl/files/$@

$(NL).unpacked: $(NL).tar.gz
	tar xzf $<
	touch $@

$(NL).built: $(NL).unpacked
	cd $(NL) && \
	./configure $(CONFIGURE_FLAGS) \
		--disable-pthreads --disable-cli && \
	make && \
	make install
	touch $@

$(NL).clean:
	rm -f $(NL).built
	rm -f $(NL).patched
	rm -f $(NL).unpacked
	rm -rf $(NL)

# Rules to build libvirt

LIBVIRT=libvirt-1.2.9

$(LIBVIRT).tar.gz:
	wget ftp://libvirt.org/libvirt/$@

$(LIBVIRT).unpacked: $(LIBVIRT).tar.gz
	tar xzf $<
	touch $@

$(LIBVIRT).patched: $(LIBVIRT).unpacked \
					libvirt-gnulib-android.patch \
				    libvirt-android.patch
	cd $(LIBVIRT) && patch -p1 <../libvirt-gnulib-android.patch
	cd $(LIBVIRT) && patch -p1 <../libvirt-android.patch
	touch $@

$(LIBVIRT).built: ndk.setup $(XDR).built $(LIBXML2).built $(LIBSSH).built $(NL).built $(LIBVIRT).patched
	cd $(LIBVIRT) && \
	LDFLAGS="-L$(BUILDDIR)/lib" CFLAGS="-I$(BUILDDIR)/include" \
	PKG_CONFIG_PATH="$(BUILDDIR)/lib/pkgconfig" \
	./configure --host=arm-linux-androideabi --prefix $(BUILDDIR) --enable-static\
				--with-pic --without-python \
				--without-test --without-gnutls \
				--without-xen --without-qemu \
				--without-openvz --without-lxc \
				--without-vbox --without-libxl \
				--without-xenapi --without-sasl \
				--without-avahi --without-polkit \
				--without-libvirtd --without-uml \
				--without-phyp --without-esx \
				--without-hyperv --without-vmware \
				--without-parallels --without-interface \
				--without-network --without-storage-fs \
				--without-storage-lvm --without-storage-iscsi \
				--without-storage-disk --without-storage-mpath \
				--without-storage-rbd --without-storage-sheepdog \
				--without-numactl --without-numad \
				--without-capng --without-selinux \
				--without-apparmor --without-udev \
				--without-yajl --without-sanlock \
				--without-libpcap --without-macvtap \
				--without-audit --without-dtrace \
				--without-driver-modules --with-init_script=redhat \
				--without-curl --without-dbus \
				--disable-werror && \
	make && \
	make install
	touch $@

$(LIBVIRT).clean:
	rm -f $(LIBVIRT).built
	rm -f $(LIBVIRT).patched
	rm -f $(LIBVIRT).unpacked
	rm -rf $(LIBVIRT)

all: $(LIBVIRT).built

clean: ndk.clean \
	   $(XDR).clean \
	   $(LIBXML2).clean \
	   $(GPGERR).clean \
	   $(GCRYPT).clean \
	   $(LIBSSH).clean \
	   $(NL).clean \
	   $(LIBVIRT).clean
