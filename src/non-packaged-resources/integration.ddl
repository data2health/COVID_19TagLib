CREATE DATABASE integration VERSION '1'
	 OPTIONS (ANNOTATION 'N3C integration', UseConnectorMetadata 'true');
USE DATABASE integration VERSION '1';

CREATE FOREIGN DATA WRAPPER postgresql;

CREATE SERVER hal FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:hal');
CREATE SERVER neuromancer FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:neuromancer');
CREATE SERVER deep_thought FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:deep-thought');
CREATE SERVER wintermute FOREIGN DATA WRAPPER postgresql OPTIONS ("resource-name" 'java:wintermute');


--
--
-- the aggregated N3C admin schemas
--
--

CREATE SCHEMA hal_admin SERVER hal OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA neuromancer_admin SERVER neuromancer OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA wintermute_admin SERVER wintermute OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'n3c_admin',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
--
--
-- the aggregated linterature schemas
--
--

CREATE SCHEMA hal_covid_pmc SERVER hal OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'covid_pmc',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA deep_thought_covid_pmc SERVER deep_thought OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'covid_pmc',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
CREATE SCHEMA neuromancer_covid_pmc SERVER neuromancer OPTIONS (
	"importer.importApproximateIndexes" 'true',
	"importer.importIndexes" 'true',
	"importer.importKeys" 'true',
	"importer.schemaPattern" 'covid_pmc',
	"importer.tableTypes" 'TABLE,VIEW'
	);
    
--
--
-- the aggregated N3C admin schemas
--
--

IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER hal INTO hal_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');

IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER neuromancer INTO neuromancer_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');

IMPORT FOREIGN SCHEMA n3c_admin FROM SERVER wintermute INTO wintermute_admin OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');

--
--
-- the aggregated linterature schemas
--
--

IMPORT FOREIGN SCHEMA covid_pmc FROM SERVER hal INTO hal_covid_pmc OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');
    
IMPORT FOREIGN SCHEMA covid_pmc FROM SERVER deep_thought INTO deep_thought_covid_pmc OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');
    
IMPORT FOREIGN SCHEMA covid_pmc FROM SERVER neuromancer INTO neuromancer_covid_pmc OPTIONS (
	"importer.tableTypes" 'TABLE,VIEW',
	"importer.importKeys" 'true',
	"importer.importIndexes" 'true',
	"importer.importApproximateIndexes" 'true');
