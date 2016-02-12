class ClassProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ISpecialCases specialCases
	AliasManager aliasManager
	PropertyProcessor propertyProcessor
	MethodProcessor methodProcessor

	def init() {

		switch( config.libraryName ) {
			case "ExtJS":
				specialCases = new SpecialCasesExtJS()
				break
			case "Touch":
				specialCases = new SpecialCasesTouch()
				break
			default:
				specialCases = new SpecialCases()
		}

		aliasManager = new AliasManager( config: config, typeManager: typeManager )
		aliasManager.init()
		propertyProcessor = new PropertyProcessor( typeManager: typeManager, config: config, definitionWriter: definitionWriter, specialCases: specialCases )
		propertyProcessor.init()
		methodProcessor = new MethodProcessor( typeManager: typeManager, config: config, definitionWriter: definitionWriter, specialCases: specialCases )
		methodProcessor.init()
	}

	def processClass( className, fileJson ) {
		Boolean hasStaticMethods = false
		def processedNames
		aliasManager.addAliases( className, fileJson )

		definitionWriter.writeToDefinition( "\texport interface I${ typeManager.getClassName( className ) } ${ typeManager.getExtends( fileJson, true ) } {" )
		definitionWriter.writeToConfig( "\texport interface I${ typeManager.getClassName( className ) }Config ${ typeManager.getExtendsConfig( fileJson, true ) } {" )
		if( !fileJson.singleton ) {
			processedNames = writeProperties( fileJson, true, false )
			hasStaticMethods = writeMethods( fileJson, processedNames, true, false )
		}
		definitionWriter.writeToConfig( "\t}" )
		definitionWriter.writeToDefinition( "\t}" )

		definitionWriter.writeToFactory( "\texport class ${ typeManager.getClassName( className ) } {" )
		definitionWriter.writeToFactory( "\t\tstatic create(config: Typ${ typeManager.getModule( className ) }.${ typeManager.getClassName( className ) }Config, extraArgs?: any) : ${ typeManager.getModule( className ) }.I${ typeManager.getClassName( className ) } {" )
		definitionWriter.writeToFactory( "\t\t\treturn TypExt.Object.create(\"" + typeManager.getModule( className ) + "." + typeManager.getClassName( className ) + "\", config, extraArgs );" )
		definitionWriter.writeToFactory( "\t\t}" )
		definitionWriter.writeToFactory( "\t}" )

//		if( !config.interfaceOnly || fileJson.singleton || hasStaticMethods ) {
//			if( !config.interfaceOnly ) {
//				definitionWriter.writeToDefinition( "\texport class ${ typeManager.getClassName( className ) } ${ typeManager.getExtends( fileJson, false ) } implements ${typeManager.getImplementedInterfaces( fileJson ) } {" )
//				processedNames = writeProperties( fileJson, false, false )
//			}
//			else {
//				definitionWriter.writeToDefinition( "\texport class ${ typeManager.getClassName( className ) } {" )
//				processedNames = [:]
//				if( fileJson.singleton ) {
//					processedNames = writeProperties( fileJson, false, false )
//				}
//			}
//
//			writeMethods( fileJson, processedNames, false, false, hasStaticMethods )
//			definitionWriter.writeToDefinition( "\t}" )
//		}
	}

	def writeProperties( fileJson, isInterface, useExport ) {
		propertyProcessor.writeProperties( fileJson, isInterface, useExport )
	}

	Boolean writeMethods( fileJson, processedNames, isInterface, useExport, staticOnly=false ) {
		return methodProcessor.writeMethods( fileJson, processedNames, isInterface, useExport, staticOnly )
	}
}
