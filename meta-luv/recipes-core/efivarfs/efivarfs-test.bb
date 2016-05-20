DESCRIPTION = "EFI varfs tests"
HOMEPAGE = "https://www.kernel.org/pub/linux/kernel"
SECTION = "base"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"
KBRANCH="stable"

# Picking up matts branch
SRC_URI = "file://bash-to-sh.patch \
          file://luv-parser-efivarfs \
          file://efivarfs"

#we need some of the stuff below
DEPENDS_class-native += "qemu-native"
SRCREV="${AUTOREV}"
inherit autotools luv-test
S = "${STAGING_KERNEL_DIR}"

RDEPENDS_${PN} += "e2fsprogs"

do_fetch[noexec] = "1"
do_unpack[depends] += "virtual/kernel:do_unpack"
do_patch[depends] += "virtual/kernel:do_shared_workdir"
do_package[depends] += "virtual/kernel:do_populate_sysroot"

EXTRA_OEMAKE = " \
    CC='${CC}' \
    -C ${S}/tools/testing/selftests/efivarfs"

# This is to just to satisfy the compilation error
#I am not sure why I am getting this
FILES_${PN}-dbg += "/usr/share/efivarfs-test/.debug"

do_configure_prepend() {
    # We need to ensure the --sysroot option in CC is preserved
    if [ -e "${S}/tools/testing/selftests/efivarfs/Makefile" ]; then
        sed -i 's,CC = $(CROSS_COMPILE)gcc,#CC,' ${S}/tools/testing/selftests/efivarfs/Makefile
    fi

    # Fix for rebuilding
    oe_runmake clean
}

#This is the compilation area
#we need to compile the self tests
do_compile() {
    unset CFLAGS
    oe_runmake
}


#Installing is nothing but putting things in place
do_install() {
    # Creating a directory
    install -d ${D}${datadir}/efivarfs-test

    #Copying some of the files, these are part of the linux code
    install -m 0755 ${S}/tools/testing/selftests/efivarfs/create-read ${D}${datadir}/efivarfs-test
    install -m 0755 ${S}/tools/testing/selftests/efivarfs/open-unlink ${D}${datadir}/efivarfs-test
    install -m 0755 ${S}/tools/testing/selftests/efivarfs/efivarfs.sh ${D}${datadir}/efivarfs-test

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/efivarfs ${D}${bindir}
}

LUV_TEST_LOG_PARSER="luv-parser-efivarfs"
LUV_TEST="efivarfs"
