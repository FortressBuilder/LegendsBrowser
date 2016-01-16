package legends.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import legends.xml.handlers.AnnotationContentHandler;

public class WorldConfig {
	private static final Log LOG = LogFactory.getLog(WorldConfig.class);

	private Path legendsPath;
	private Path legendsPlusPath;
	private Path worldGenPath;
	private Path historyPath;
	private Path sitesAndPropsPath;
	private Path imagePath;

	public WorldConfig(Path path) throws IOException {
		if (path.toString().endsWith(".xml")) {
			worldGenPath = makeWorldGenPath(path);
			legendsPath = path;
			legendsPlusPath = makeLegendsPlusPath(path);
			historyPath = makeHistoryPath(path);
			sitesAndPropsPath = makeSitesAndPropsPath(path);

			Path dir = path.getParent();
			String prefix = path.getFileName().toString().replace("-legends.xml", "");
			// try to load images (priorize detailed)
			Files.newDirectoryStream(dir, prefix + "-world_map.*").forEach(this::setImagePath);
			Files.newDirectoryStream(dir, prefix + "-detailed.*").forEach(this::setImagePath);
		} else if (path.toString().endsWith(".zip")) {
			Map<String, String> env = new HashMap<>();
			env.put("create", "false");

			URI uri = URI.create("jar:" + path.toUri());
			LOG.info(uri);
			FileSystem fs;
			try {
				fs = FileSystems.getFileSystem(uri);
			} catch (FileSystemNotFoundException e) {
				fs = FileSystems.newFileSystem(uri, env);
			}

			Path baseDir = path.getParent();
			Path dir = fs.getPath("/");

			// try to load world_gen_param.txt from base directory
			String region = path.getFileName().toString().substring(0, path.getFileName().toString().indexOf("-"));
			Files.newDirectoryStream(baseDir, region + "-world_gen_param.txt").forEach(p -> worldGenPath = p);

			// try to load files from archive
			Files.list(dir).forEach(p -> {
				String s = p.toString();
				if (s.endsWith("-legends.xml"))
					legendsPath = p;
				else if (s.endsWith("-legends_plus.xml"))
					legendsPlusPath = p;
				else if (s.endsWith("-world_history.txt"))
					historyPath = p;
				else if (s.endsWith("-world_sites_and_pops.txt"))
					sitesAndPropsPath = p;
				else if (s.endsWith("-world_sites_and_pops.txt"))
					sitesAndPropsPath = p;
				else if (s.endsWith("-world_gen_param.txt"))
					worldGenPath = p;
			});

			String prefix = path.getFileName().toString().replace("-legends_archive.zip", "");
			// try to load images from archive (priorize detailed)
			Files.newDirectoryStream(dir, prefix + "*-world_map.*").forEach(this::setImagePath);
			Files.newDirectoryStream(dir, prefix + "*-detailed.*").forEach(this::setImagePath);
		}
	}

	private Path makeSitesAndPropsPath(Path path) {
		String cp = path.getFileName().toString().toLowerCase();
		return path.resolveSibling(cp.substring(0, cp.lastIndexOf("-")) + "-world_sites_and_pops.txt");
	}

	private Path makeHistoryPath(Path path) {
		String cp = path.getFileName().toString().toLowerCase();
		return path.resolveSibling(cp.substring(0, cp.lastIndexOf("-")) + "-world_history.txt");
	}

	private Path makeLegendsPlusPath(Path path) {
		String cp = path.getFileName().toString().toLowerCase();
		return path.resolveSibling(cp.substring(0, cp.lastIndexOf("-")) + "-legends_plus.xml");
	}

	private Path makeWorldGenPath(Path path) {
		String cp = path.getFileName().toString().toLowerCase();
		return path.resolveSibling(cp.substring(0, cp.lastIndexOf("-") - 12) + "-world_gen_param.txt");
	}

	public Path getLegendsPath() {
		return legendsPath;
	}

	public Path getLegendsPlusPath() {
		return legendsPlusPath;
	}

	public Path getWorldGenPath() {
		return worldGenPath;
	}

	public Path getHistoryPath() {
		return historyPath;
	}

	public Path getSitesAndPropsPath() {
		return sitesAndPropsPath;
	}

	public Path getImagePath() {
		return imagePath;
	}

	private void setImagePath(Path imagePath) {
		this.imagePath = imagePath;
	}

	public boolean plusAvailable() {
		try {
			return legendsPath != null && Files.exists(legendsPlusPath);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("legendsPath:       ").append("" + legendsPath).append("\n");
		sw.append("legendsPlusPath:   ").append("" + legendsPlusPath).append("\n");
		sw.append("worldGenPath       ").append("" + worldGenPath).append("\n");
		sw.append("historyPath:       ").append("" + historyPath).append("\n");
		sw.append("sitesAndPropsPath: ").append("" + sitesAndPropsPath).append("\n");
		sw.append("imagePath:         ").append("" + imagePath).append("\n");
		return sw.toString();
	}

	public static boolean isLegendsFile(Path p) {
		String n = p.getFileName().toString();
		if (n.endsWith("-legends.xml"))
			return true;
		if (n.endsWith("-legends_archive.zip"))
			return true;
		return false;
	}
}