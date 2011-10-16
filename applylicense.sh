#!/bin/sh
copyrightNotice() cat << EOF
BEGIN COPYRIGHT NOTICE

Copyright [2011] [Rodrigo Lemos]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

END COPYRIGHT NOTICE
EOF

# the following files should not have copyright notice
# (generally because they are distributed under another license)
EXCLUSIONS=`cat << EOF
LICENSE.txt
applylicense.sh
EOF`

NOTICEMARKERSREGEXP="\(BEGIN\|END\) COPYRIGHT NOTICE"

HASHNOTICE="`mktemp -t noticeXXXXX`"
JAVANOTICE="`mktemp -t noticeXXXXX`"
XMLNOTICE="`mktemp -t noticeXXXXX`"

trap "rm -fR $HASHNOTICE $XMLNOTICE $JAVANOTICE" exit

(
	copyrightNotice | sed -e 's/^/# /'
) > "$HASHNOTICE"

(
	echo "<!--"
	copyrightNotice | sed -e 's/^/  /'
	echo "-->"
) > "$XMLNOTICE"

(
	head -c 80 < /dev/zero | tr '\0' '*' | sed -e 's/^*/\//' -e 's/$/\n/'
	copyrightNotice | sed -e 's/^/ * /'
	head -c 80 < /dev/zero | tr '\0' '*' | sed -e 's/^*/ /' -e 's/*$/\/\n/'
) > "$JAVANOTICE"

findPreviousLicense() {
	FILE="$1"
	GREPOUTPUT=`grep "$NOTICEMARKERSREGEXP" -Zno "$FILE"` || return 1;
	echo "$GREPOUTPUT" | sed -e "s/:$NOTICEMARKERSREGEXP//g" | tr "\n" " "
}

stripPreviousLicense() {
	FILE="$1"
	INCRBEGIN="$2"
	INCREND="$3"

	LINES="`findPreviousLicense "$FILE"`" || return
	set -- $LINES; BEGIN="$1"; END="$2"

	BEGIN="$(($BEGIN $INCRBEGIN))"
	END="$(($END $INCREND))"

	sed -e "${BEGIN},${END}d" -i "$FILE"
}

stuffFirstLine() {
	FILE="$1"
	sed -i "$FILE" -f - << EOF
1i\
_
EOF
}

applyJava() {
	FILE="$1"
	stripPreviousLicense "$FILE" "-1" "+1"

	stuffFirstLine "$FILE"
	sed -i "$FILE" -e "1r $JAVANOTICE" -e "1d"
}

applyXML() {
	FILE="$1"
	stripPreviousLicense "$FILE" "-1" "+1"

	# aaa aaa dd dd:dd:dd aaa dddd
	if (head -n 1 "$FILE" | grep -q "<?") then
		sed -i "$FILE" -e "1r $XMLNOTICE"
	else
		stuffFirstLine "$FILE"
		sed -i "$FILE" -e "1r $XMLNOTICE" -e "1d"
	fi
}

applyHash() {
	FILE="$1"
	stripPreviousLicense "$FILE"

	# aaa aaa dd dd:dd:dd aaa dddd
	if (head -n 1 "$FILE" | grep -q "^#... ... .. ..:..:.. ..S\?. ....$") then
		sed -i "$FILE" -e "1r $HASHNOTICE"
	elif (head -n 1 "$FILE" | grep -q "^#!") then
		sed -i "$FILE" -e "1r $HASHNOTICE"
	else
		stuffFirstLine "$FILE"
		sed -i "$FILE" -e "1r $HASHNOTICE" -e "1d"
	fi
}

EXCLUSIONS="`echo "$EXCLUSIONS" | sed -e "s|^|./|"`"

find \( -path './.git' -o -path '*/target' \) -prune -o -type f -print | grep -vF "$EXCLUSIONS" |
while read FILE
do
	
	case "`basename "$FILE"`" in
		*.java | *.aj)
			applyJava "$FILE"
			;;
		*.xml | *.xsd)
			applyXML "$FILE"
			;;
		*.ad)
			# ignore
			;;
		*)
			MAGIC="`file -b "$FILE"`"
			case "$MAGIC" in
				"XML  document text" | "XML document text" )
					applyXML "$FILE"
					;;
				"ASCII text" | "POSIX shell script text executable")
					applyHash "$FILE"
					;;
				*)
					# don't know
					echo "$FILE: $MAGIC"
					;;
			esac
			;;
	esac
done

