## Copyright (C) 1999-2005 Open Source Telecom Corporation.
## Copyright (C) 2006-2008 David Sugar, Tycho Softworks.
## Copyright (C) 2009-2010 Werner Dittmann
##
## This file is free software; as a special exception the author gives
## unlimited permission to copy and/or distribute it, with or without
## modifications, as long as this notice is preserved.
##
## This program is distributed in the hope that it will be useful, but
## WITHOUT ANY WARRANTY, to the extent permitted by law; without even the
## implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#

cmake_minimum_required(VERSION 2.6)

PROJECT(libccrtp1)

# for debug and trace during CMakeList development
set(CMAKE_VERBOSE_MAKEFILE FALSE)

# set some global variables, in particular version variables
set(VERSION 1.7.1)
set(LT_RELEASE 1.7)
set(REQUIRES 1.3.0)

# find project specific modules
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")

# Set library postfix in case of 64 bit architecture
if (${CMAKE_SYSTEM_PROCESSOR} STREQUAL "x86_64")
  set(LIB64 64)
endif() 

include(AutoArgs)
enable_arg(srtp true)

# include all the fine stuff we need 
include(FindGcryptConfig)
include(FindPkgConfig)
include(CheckLibraryExists)
include(CheckIncludeFiles)

# setup the Thread include and lib
find_package(Threads)
if (CMAKE_HAVE_PTHREAD_H)
    set(HAVE_PTHREAD_H TRUE)
endif()
set (LIBS ${LIBS} ${CMAKE_THREAD_LIBS_INIT})

pkg_check_modules(COMMON libccgnu2>=${REQUIRES})
set (LIBS ${LIBS} ${COMMON_LDFLAGS})

if (enable_srtp)
  # now get info about crypto libraries
  gcr_check(GCRYPT gcrypt)
  if (GCRYPT_FOUND)
    check_include_files(gcrypt.h HAVE_GCRYPT_H)
    set(LIBS ${LIBS} ${GCRYPT_LIBRARIES})
  else ()
    pkg_check_modules(OPENSSL libssl>=0.9.8)
    if (OPENSSL_FOUND) 
      check_include_files(openssl/bn.h HAVE_OPENSSL_BN_H)
      check_include_files(openssl/aes.h HAVE_OPENSSL_AES_H)
      check_include_files(openssl/sha.h HAVE_OPENSSL_SHA_H)
      check_library_exists(crypto EVP_CipherInit_ex "" HAVE_SSL_CRYPT)
      set(LIBS ${LIBS} -lcrypto)
    else()
      message(FATAL_ERROR "No crypto library found")
    endif()
  endif()
  set(SRTP_SUPPORT TRUE)
endif()

# the following set(...) commands are only to have backward
# compatibility with autoconf stuff to generate the pc file
set(prefix ${CMAKE_INSTALL_PREFIX})
set(exec_prefix ${prefix}/bin)
set(libdir ${prefix}/lib)
set(includedir ${prefix}/include)
set(PACKAGE pkgconfig)
configure_file(libccrtp1.pc.in ${CMAKE_CURRENT_BINARY_DIR}/libccrtp1.pc @ONLY)

include(SourceDistribution)
src_distribution_init()
add_src_dist_files(AUTHORS COPYING INSTALL NEWS README ChangeLog 
                   libccrtp1.pc.in ccrtp.spec.cmake COPYING.addendum)

add_definitions(-g)
if(CMAKE_COMPILER_IS_GNUCXX)
  add_definitions(-Wno-long-long -Wno-char-subscripts)
  add_definitions(-Wall -ansi -pedantic)
endif()

# now prepare and go for the sub-directories
include_directories(${CMAKE_CURRENT_BINARY_DIR}/src ${CMAKE_SOURCE_DIR}/src)

add_subdirectory(src)
add_src_dist_dirs(src)

add_subdirectory(demo)
add_src_dist_dirs(demo)

#add_subdirectory(doc)
#add_src_dist_dirs(doc)

########### install files ###############
install(FILES ${CMAKE_CURRENT_BINARY_DIR}/libccrtp1.pc DESTINATION lib${LIB64}/pkgconfig)

########### Add uninstall target ###############
configure_file(
  "${CMAKE_CURRENT_SOURCE_DIR}/cmake/cmake_uninstall.cmake.in"
  "${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake"
   IMMEDIATE @ONLY)
add_custom_target(uninstall
  "${CMAKE_COMMAND}" -P "${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake") 

####### define target to build RPM 
include(UseRPMTools)
if(RPMTools_FOUND)
  RPMTools_ADD_RPM_TARGETS(${PROJECT_NAME} ${CMAKE_SOURCE_DIR}/ccrtp.spec.cmake)
endif()