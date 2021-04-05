CREATE DATABASE integration VERSION '1'
	 OPTIONS (ANNOTATION 'N3C integration', UseConnectorMetadata 'true');
USE DATABASE integration VERSION '1';

CREATE FOREIGN DATA WRAPPER postgresql;

CREATE SERVER "hal" FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:hal');
CREATE SERVER "neuromancer" FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:neuromancer');
CREATE SERVER "deep-thought" FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:deep-thought');
CREATE SERVER "wintermute" FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:wintermute');

CREATE SCHEMA hal_admin SERVER "hal" OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA neuromancer_admin SERVER "neuromancer" OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA wintermute_admin SERVER "wintermute" OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER "hal" INTO hal_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');

IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER "neuromancer" INTO neuromancer_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');

IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER "wintermute" INTO wintermute_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');
