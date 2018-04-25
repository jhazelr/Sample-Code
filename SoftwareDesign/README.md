Link to the latest design for this project: 
	
	https://ada.csse.rose-hulman.edu/terribledesigners/RevEngD/blob/master/design/M4/design.svg

Instructions:
1. Download the project as a zip folder from the following address:

	https://ada.csse.rose-hulman.edu/terribledesigners/RevEngD

2. Unzip the project folder to your desired location, and then import the project as
	a gradle project into Eclipse.
	
3. Open a command terminal in your machine and change directory to the parent folder of the project.

4. To run the project, type "java -cp release/extensible-project.jar csse374.revengd.app.Application <properties file>", where <properties file> is the complete file path of the properties file
you want to run the project based on. If <properties file> is not specified, the default properties file, src/main/java/csse374/revengd/app/settings/default.properties, will be used.

	java -cp release/extensible-project.jar csse374.revengd.app.Application

or

	java -cp release/extensible-project.jar csse374.revengd.app.Application C:/EclipseWorkspaces/csse374/TermProject/RevEngDProject/src/main/java/csse374/revengd/app/settings/decorator.properties

5. An SVG file is generated as an output of the project. It can be found under the "diagrams" folder. You can also find the textual code for the svg file under the "plant_uml_code"
folder.

6. An sample properties file is provided under the following link. More detail instructions are specified below.
	
	https://ada.csse.rose-hulman.edu/terribledesigners/RevEngD/blob/master/example.properties

	 

The command line will only have one flag and that flags purpose is to pass in a settings file and that flag is:

-sfile

For the -sfile flag is there to indicate which settings file will be used and it requires the full path of the file. 
This is not a mandatory flag however because if it's not included the project will simply use its default properties file.

	ex: -sfile = /Users/username/Documents/TestWorkspace/class/project/projectname/src/project/src/app/filename.properties



All flags are below this point are in the Settings(properties) files and NOT in the command line : 

For out project to run you have a few flags you must include:
 The mandatory flags that must be included are:

-dir
-entry

For -dir it must be follows with a space and then the directory path to the project.
	
	ex: -dir = /Users/username/Documents/TestWorkspace/class/project/projectname

For -entry it must be followed by the project's entry class.

	ex: -entry = csse374.revengd.app.Application

The following flags are optional in our project : 

-class
-r
-p
-save
-sd
-sdsave
-sdn
-exc
-irt
-aAlg
-syn
-blist
-folder
-pdetecotrs
-afilters
-adapterRatio
-aTransformers


For -class you specify the list of classes that are you want to include in this project. If you don't, specify a class however nothing will be shown in your picture. Therefore you should specify a class that you want your project to include with a comma and no spaces separating them.

	ex: -class = <your package path>.<your class name>,Java.lang.String

If the -r flag is included the program will recursively include all super classes and interfaces of your program. 
	
	ex: -r = true

For the -p flag, you follow it by the privacy you want it to be. The privacy options are public, private, protected

	ex: -p = public

For the -save flag, it indicates where you want to save your file. If you don't indicate it, it will default and may overwrite any old picture.

	ex: -save = Users/username/Documents/TestWorkspace/class/project/ex.png

The -sd flag in the program allows you to be able to successfully create a sequence diagram for what project you specify by including the entering method you would like the sequence diagram to show
	
	ex: -sd= headfirst.designpatterns.observer.weather.WeatherStation.main()
	
The -sdn flag allows you to control the depth of of the sequence diagram and how deep each function will go. If the flag is not put in, the program will default to 5.

	ex: -sdn = 5
	
The -sdsave like the -save flag indicates where you want to save your file. If you don't indicate it, it will default and may overwrite any old picture.

	ex: -sdsave = Users/username/Documents/TestWorkspace/class/project/ex.png
	
The -exc flag will not expand any java library classes.
	
	ex: -exc = true
	
The -irt flag is made to determine which Resolution strategy the user would like to use with there algorithm. They have 4 options which are the single algorithm, Union of multiple algorithms,  Intersection of multiple algorithms, or a chain which will return the first result set of the algorithms.
 *DISCLAMIR* if the -irt flag is not passed in. the program will run a single Hierarchy Algorithm

 	ex: -irt = csse374.revengd.app.algorithms.HierarchyAlgorithm
  	
 The -aAlg flags are to indicate which algorithm are to be included. If you're running a single algorithm only needs one of the algorithms. You are to to put all the algorithms separated by a comma.
 *DISCLAMIR* the program will not run if the -irt flag is given and no algorithm is passed in. 
	
	ex: -aAlg=csse374.revengd.app.algorithms.HierarchyAlgorithm,csse374.revengd.app.algorithms.CallGraphAlgorithm

For the -syn flag the software will end up excluding all synthetic methods that are in the project
	
	ex: -syn = synthetic
	
For the -blist flag is for black listed files and you use this flag by having all the prefixes of the classes seperated by a comma.
	
	ex: -blist = java.,javax.

For the -folder flag you will be able to include all classes that are located in the full directory.
	
	ex: -folder = /Users/username/Documents/TestWorkspace/class/project/projectname/src/project/src/app/
	
For the -pdetectors flag allows the user to load up detectors from outside classes. You run it by having the transformer followed by a ":"   and the name of the pattern that you would like to call and then separated from the renders by a ";" and that is one unit. The units are separated out by a ",".
	
	ex: -pdetectors = csse374.revengd.app.transformer.SingletonTransformer:Singleton;csse374.revengd.app.renderer.SingletonRenderer

 The -adapterRatio flag is used ff the -pdetecotors include an AdapterTransformer. If it does you have the option of including the ratio of how similar the adaptor is to its superclass and using a single object. This is done using the flag of -adapterRatio. The flags run from 0.0-1.0. If the flag is not included, it will default to 0.75. 
 	
 	ex: -adapterRatio: 0.5

For the -afilters flag, you are able to add filters during runtime. This is done by having all the IFilters classes separated by a comma.

	ex: -afilters= csse37.revengd.app.filter.PrivateFilter,csse37.revengd.app.filter.ProtectedFilter
	
For the -aTransformers flag, you are able to add Transformers that are not detectors during runtime. This is done by having all the Transformers classes separated by a comma.

	ex: -aTransformers= csse37.revengd.app.transformer.Additonal1,csse37.revengd.app.transformer.Additonal2
	

For the -aTransformers flag, you are able to add Transformers that are not detectors during runtime. This is done by having all the Transformers classes separated by a comma.

	ex: -aTransformers= csse37.revengd.app.transformer.Additonal1,csse37.revengd.app.transformer.Additonal2	

Individual contributions:

George: Created the logic for the main class, preprocessor class, and the filters. Also wrote all the sequence diagram logic. He also wrote all the algorithm logic and flag to run the program. Geroge Also wrote all the logic for detecting the adapter and dependency inversion patterns;

Yizhi: Implemented the logic for configuring and analyzing dependencies; Implemented the logic for rendering the code for PlantUML; Implemented AssociationTransformer. Implemented the detectors transformers and the patterns; Implemented decorator pattern detecting transformer; created all properties files.

Jordan: Coded the PrivacyTransformer, Worked with Yizhi on the RecursiveTransformer, Helped with logic is RenderingTransformer, Worked on DependencyTransformer. Jordan worked on implementing the settings file and refactoring the database file. Implemented Decorator Pattern Detecting Transformer

Equal participation by all members: debugging, building, and making the design.
