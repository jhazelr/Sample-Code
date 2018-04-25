package csse374.revengd.app;

import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.io.FileUtils;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class PlantUmlMaker implements Runnable {

	private String dir;
	private static String codeString = "./plant_uml_code/code.txt/";
	private static String sdCode = "./plant_uml_code/SDcode.txt/";
	private static final Logger logger = LogManager.getLogger(".");

	public PlantUmlMaker(String dir, boolean makeSD) {
		this.dir = dir;
		if (makeSD) {
			codeString = sdCode;
		}
	}

	public PlantUmlMaker(boolean makeSD) throws IOException {
		Path filePath = Paths.get(System.getProperty("user.dir"), "build", "plantuml", "diagram.png");
		Files.createDirectories(filePath.getParent());

		this.dir = filePath.toString();
		if (makeSD) {
			codeString = sdCode;
		}
	}

	public void run() {
		try {
			String code = FileUtils.readFileToString(new File(codeString), StandardCharsets.UTF_8);
			SourceStringReader reader = new SourceStringReader(code);

			OutputStream outStream = new FileOutputStream(dir);
			FileFormatOption option = new FileFormatOption(FileFormat.SVG, false);
			DiagramDescription description = reader.outputImage(outStream, option);
			logger.info("UML diagram generated at: " + dir);
			logger.info(description);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
