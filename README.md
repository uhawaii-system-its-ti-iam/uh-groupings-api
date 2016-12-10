# UHGroupingsAPI
This API is intended to connect UH Groupings to UH Grouper. It
will do this by using the Grouper API for Grouper Web Services
through the Grouper Client tool. It will also make the Grouper
functions easier to use for the end user by only requiring from 
them a username, password.

Grouper is a tool that lets someone create and manage groups.
https://spaces.internet2.edu/display/Grouper
	The installer can be downloaded form here.
	https://software.internet2.edu/grouper/release/2.3.0/grouper.installer-2.3.0.tar.gz

Grouper Client is a tool that uses a Java library in the form of
a .jar file to manipulate Grouper from the command line or from 
a Java program. It is intended to be backwards compatable and will
be able to be updated with little or no reconfiguration to the 
Groupings program.
https://spaces.internet2.edu/display/Grouper/Grouper+Client
	It can be downloaded from here.
	https://software.internet2.edu/grouper/release/2.3.0/grouper.clientBinary-2.3.0.tar.gz

Groupings is a similar tool that uses Grouper as its back end. 
Groupings is meant to focus more on ease of use and less on 
extra functions. 

Methods:

	MyGroups
	GroupsIOwn
	

	AddGrouping
	DeleteGrouping
	
	AssignOwner
	GetOwners
	AssignAttributes

	AddMember
	DeleteMember
	GetMembers

	
