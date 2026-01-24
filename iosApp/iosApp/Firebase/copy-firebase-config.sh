#!/bin/bash

# Build Phase Script for copying Firebase configuration
# Add this as a "Run Script" phase in Xcode (Build Phases)
#
# To add in Xcode:
# 1. Select your target > Build Phases
# 2. Click + > New Run Script Phase
# 3. Rename it to "Copy Firebase Config"
# 4. Paste the content below (without the comments)
# 5. Move it BEFORE "Compile Sources" phase

if [ "${CONFIGURATION}" == "Debug" ]; then
    echo "Copying DEBUG Firebase config..."
    cp "${SRCROOT}/iosApp/Firebase/Debug/GoogleService-Info.plist" "${BUILT_PRODUCTS_DIR}/${PRODUCT_NAME}.app/"
else
    echo "Copying RELEASE Firebase config..."
    cp "${SRCROOT}/iosApp/Firebase/Release/GoogleService-Info.plist" "${BUILT_PRODUCTS_DIR}/${PRODUCT_NAME}.app/"
fi

echo "Firebase config copied successfully for ${CONFIGURATION} build"
