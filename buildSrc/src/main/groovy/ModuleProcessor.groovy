class ModuleProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ClassProcessor classProcessor

	def init() {
		definitionWriter = new DefinitionWriter( config: config )
		definitionWriter.init()
		classProcessor = new ClassProcessor( typeManager: typeManager, config: config, definitionWriter: definitionWriter )
		classProcessor.init()
	}

	def processModule( className, fileJson, processedClasses ) {
		config.currentModule = typeManager.getModule( className )

		// processedClasses hashmap is used to remove duplicated class definition in generated d.ts
		// added by JeT nov 2015 
		if ( processedClasses[className] != "done" ) {
			println "* Process class " + className 
			definitionWriter.writeToDefinition( "declare module ${ typeManager.getModule( className ) } {" )
			definitionWriter.writeToFactoryDefinition( "declare module Typ${ typeManager.getModule( className ) } {" )
			definitionWriter.writeToConfig( "declare module Typ${ typeManager.getModule( className ) } {" )
			definitionWriter.writeToFactory( "module Typ${ typeManager.getModule( className ) } {" )
			def processedNames
	
			// Ext class has special handling to turn it into Ext module-level properties and methods.
			if( className == "Ext" ) {
				processedNames = classProcessor.writeProperties( fileJson, false, true )
				classProcessor.writeMethods( fileJson, processedNames, false, true )
			}
			else {
				classProcessor.processClass( className, fileJson )
			}
	
			definitionWriter.writeToFactory( "}" )
			definitionWriter.writeToFactory( "" )
			definitionWriter.writeToFactoryDefinition( "}" )
			definitionWriter.writeToFactoryDefinition( "" )
			definitionWriter.writeToConfig( "}" )
			definitionWriter.writeToConfig( "" )
			definitionWriter.writeToDefinition( "}" )
			definitionWriter.writeToDefinition( "" )
			processedClasses[className] = "done"
		}
	}
}
