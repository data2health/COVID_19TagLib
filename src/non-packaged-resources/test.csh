#!/bin/csh

echo ""
echo n3c.cd2h.org...
echo ""
rsync -avt  workspace/n3c-dashboard/WebContent/ harlie:tomcat/dashboard/apache-tomcat-9.0.54/webapps/dashboard/

echo ""
echo iowa.cd2h.org...
echo ""
rsync -avt  workspace/n3c-dashboard/WebContent/ guardian:tomcat/dashboard/apache-tomcat-9.0.0.M9/webapps/dashboard

foreach a (*)
	echo $a
end

