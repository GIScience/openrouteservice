Name: openrouteservice
Version: 7.1.0
Release: 1
Summary: openrouteservice
License: GPL 3
BuildArch: noarch
#Requires: jboss-tomcat8

%description
The openrouteservice API provides global spatial services by consuming user-generated and collaboratively collected free geographic data directly from http://www.openstreetmap.org.
It is highly customizable, performant and written in Java.

%install
mkdir -p $RPM_BUILD_ROOT/opt/ors
cp %{_sourcedir}/ors.war $RPM_BUILD_ROOT/opt/ors/

%post
# not systemctl start jboss-tomcat8

%files

%defattr(-,root,root)
/opt/ors/ors.war