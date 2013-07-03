package us.talabrek.ultimateskyblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;

public class Metrics {
	private static final int REVISION = 6;
	private static final String BASE_URL = "http://mcstats.org";
	private static final String REPORT_URL = "/report/%s";
	private static final int PING_INTERVAL = 10;
	private final Plugin plugin;
	private final YamlConfiguration configuration;
	private final File configurationFile;
	private final String guid;
	private final boolean debug;
	/* 101 */private final Object optOutLock = new Object();

	/* 106 */private volatile BukkitTask task = null;

	public Metrics(Plugin plugin) throws IOException {
		/* 109 */if (plugin == null) {
			/* 110 */throw new IllegalArgumentException("Plugin cannot be null");
		}

		/* 113 */this.plugin = plugin;

		/* 116 */this.configurationFile = getConfigFile();
		/* 117 */this.configuration = YamlConfiguration.loadConfiguration(this.configurationFile);

		/* 120 */this.configuration.addDefault("opt-out", Boolean.valueOf(false));
		/* 121 */this.configuration.addDefault("guid", UUID.randomUUID().toString());
		/* 122 */this.configuration.addDefault("debug", Boolean.valueOf(false));

		/* 125 */if (this.configuration.get("guid", null) == null) {
			/* 126 */this.configuration.options().header("http://mcstats.org").copyDefaults(true);
			/* 127 */this.configuration.save(this.configurationFile);
		}

		/* 131 */this.guid = this.configuration.getString("guid");
		/* 132 */this.debug = this.configuration.getBoolean("debug", false);
	}

	public boolean start() {
		/* 143 */synchronized (this.optOutLock) {
			/* 145 */if (isOptOut()) {
				/* 146 */return false;
			}

			/* 150 */if (this.task != null) {
				/* 151 */return true;
			}

			/* 155 */this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable() {
				/* 157 */private boolean firstPost = true;

				public void run() {
					try {
						/* 162 */synchronized (Metrics.this.optOutLock) {
							/* 164 */if ((Metrics.this.isOptOut()) && (Metrics.this.task != null)) {
								/* 165 */Metrics.this.task.cancel();
								/* 166 */Metrics.this.task = null;
							}

						}

						/* 173 */Metrics.this.postPlugin(!this.firstPost);

						/* 177 */this.firstPost = false;
					} catch (IOException e) {
						/* 179 */if (Metrics.this.debug)
							/* 180 */Bukkit.getLogger().log(Level.INFO, "[Metrics] " + e.getMessage());
					}
				}
			}, 0L, 12000L);

			/* 186 */return true;
		}
	}

	public boolean isOptOut() {
		/* 196 */synchronized (this.optOutLock) {
			try {
				/* 199 */this.configuration.load(getConfigFile());
			} catch (IOException ex) {
				/* 201 */if (this.debug) {
					/* 202 */Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
				}
				/* 204 */return true;
			} catch (InvalidConfigurationException ex) {
				/* 206 */if (this.debug) {
					/* 207 */Bukkit.getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
				}
				/* 209 */return true;
			}
			/* 211 */return this.configuration.getBoolean("opt-out", false);
		}
	}

	public void enable() throws IOException {
		/* 222 */synchronized (this.optOutLock) {
			/* 224 */if (isOptOut()) {
				/* 225 */this.configuration.set("opt-out", Boolean.valueOf(false));
				/* 226 */this.configuration.save(this.configurationFile);
			}

			/* 230 */if (this.task == null)
				/* 231 */start();
		}
	}

	public void disable() throws IOException {
		/* 243 */synchronized (this.optOutLock) {
			/* 245 */if (!isOptOut()) {
				/* 246 */this.configuration.set("opt-out", Boolean.valueOf(true));
				/* 247 */this.configuration.save(this.configurationFile);
			}

			/* 251 */if (this.task != null) {
				/* 252 */this.task.cancel();
				/* 253 */this.task = null;
			}
		}
	}

	public File getConfigFile() {
		/* 269 */File pluginsFolder = this.plugin.getDataFolder().getParentFile();

		/* 272 */return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
	}

	private void postPlugin(boolean isPing) throws IOException {
		/* 280 */PluginDescriptionFile description = this.plugin.getDescription();
		/* 281 */String pluginName = description.getName();
		/* 282 */boolean onlineMode = Bukkit.getServer().getOnlineMode();
		/* 283 */String pluginVersion = description.getVersion();
		/* 284 */String serverVersion = Bukkit.getVersion();
		/* 285 */int playersOnline = Bukkit.getServer().getOnlinePlayers().length;

		/* 290 */StringBuilder data = new StringBuilder();

		/* 293 */data.append(encode("guid")).append('=').append(encode(this.guid));
		/* 294 */encodeDataPair(data, "version", pluginVersion);
		/* 295 */encodeDataPair(data, "server", serverVersion);
		/* 296 */encodeDataPair(data, "players", Integer.toString(playersOnline));
		/* 297 */encodeDataPair(data, "revision", String.valueOf(6));

		/* 300 */String osname = System.getProperty("os.name");
		/* 301 */String osarch = System.getProperty("os.arch");
		/* 302 */String osversion = System.getProperty("os.version");
		/* 303 */String java_version = System.getProperty("java.version");
		/* 304 */int coreCount = Runtime.getRuntime().availableProcessors();

		/* 307 */if (osarch.equals("amd64")) {
			/* 308 */osarch = "x86_64";
		}

		/* 311 */encodeDataPair(data, "osname", osname);
		/* 312 */encodeDataPair(data, "osarch", osarch);
		/* 313 */encodeDataPair(data, "osversion", osversion);
		/* 314 */encodeDataPair(data, "cores", Integer.toString(coreCount));
		/* 315 */encodeDataPair(data, "online-mode", Boolean.toString(onlineMode));
		/* 316 */encodeDataPair(data, "java_version", java_version);

		/* 319 */if (isPing) {
			/* 320 */encodeDataPair(data, "ping", "true");
		}

		/* 324 */URL url = new URL("http://mcstats.org" + String.format("/report/%s", new Object[] { encode(pluginName) }));
		URLConnection connection;
		/* 331 */if (isMineshafterPresent())
			/* 332 */connection = url.openConnection(Proxy.NO_PROXY);
		else {
			/* 334 */connection = url.openConnection();
		}

		/* 337 */connection.setDoOutput(true);

		/* 340 */OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		/* 341 */writer.write(data.toString());
		/* 342 */writer.flush();

		/* 345 */BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		/* 346 */String response = reader.readLine();

		/* 349 */writer.close();
		/* 350 */reader.close();

		/* 352 */if ((response == null) || (response.startsWith("ERR")))
			/* 353 */throw new IOException(response);
	}

	private boolean isMineshafterPresent() {
		try {
			/* 364 */Class.forName("mineshafter.MineServer");
			/* 365 */return true;
		} catch (Exception e) {
		}
		/* 367 */return false;
	}

	private static void encodeDataPair(StringBuilder buffer, String key, String value) throws UnsupportedEncodingException {
		/* 385 */buffer.append('&').append(encode(key)).append('=').append(encode(value));
	}

	private static String encode(String text) throws UnsupportedEncodingException {
		/* 395 */return URLEncoder.encode(text, "UTF-8");
	}
}