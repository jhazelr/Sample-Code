package csse374.revengd.examples.driver;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

/**
 * This is a short example that demonstrates how to programmatically generate
 * PNG (or other formats) output from a given PlantUML source.
 * 
 * NOTE: You will need to install Graphviz on your machine for this example to
 * work. Here is the URL: http://www.graphviz.org/download/
 */
public class E5PlantUMLGenerator implements Runnable {
	private static final Logger logger = LogManager.getLogger(E5PlantUMLGenerator.class.getName());

	@Override
	public void run() {
		StringBuilder umlSource = new StringBuilder();
		umlSource.append("@startuml\n");
		umlSource.append("Alice <|-- Bob\n");
		umlSource.append("@enduml\n");

		SourceStringReader reader = new SourceStringReader(umlSource.toString());
		try {
			Path filePath = Paths.get(System.getProperty("user.dir"), "build", "plantuml", "diagram.svg");
			Files.createDirectories(filePath.getParent());

			OutputStream outStream = new FileOutputStream(filePath.toFile());
			FileFormatOption option = new FileFormatOption(FileFormat.SVG, false);
			DiagramDescription description = reader.outputImage(outStream, option);
			logger.info("UML diagram generated at: " + filePath.toString());
			logger.info(description);
		} catch (Exception e) {
			logger.error("Cannot create file to store the UML diagram.", e);
		}
	}
}
