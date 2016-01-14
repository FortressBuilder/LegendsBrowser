package legends.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import legends.helper.EventHelper;
import legends.model.collections.OccasionCollection;
import legends.model.collections.WarCollection;
import legends.model.events.basic.Coords;
import legends.model.events.basic.Filters;
import legends.xml.annotation.Xml;

public class Entity extends AbstractObject {
	@Xml("name")private String name;

	@Xml("race")private String race = "unknown";
	@Xml("type")private String type = "unknown";
	private Set<Site> sites = new LinkedHashSet<>();
	private Entity parent;
	private List<Leader> leaders = new ArrayList<>();
	private List<Integer> children = new ArrayList<>();
	private List<EntityLink> entityLinks = new ArrayList<>();
	private Map<Integer, EntityPosition> positions = new HashMap<>();
	private Map<Integer, EntityPositionAssignment> assignments = new HashMap<>();
	private List<Integer> hfIds = new ArrayList<>();
	private List<Integer> worshipIds = new ArrayList<>();
	private List<Coords> claims = new ArrayList<>();

	private boolean fallen = false;

	public String getName() {
		return EventHelper.name(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRace() {
		return race;
	}

	public void setRace(String race) {
		this.race = race;
	}

	public Entity getRoot() {
		if (id == -1)
			return this;
		if (parent == null)
			return this;
		else
			return parent.getRoot();
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity parent) {
		if (id != -1 && parent.id != -1 && this != parent && parent != null)
			this.parent = parent;
	}

	public Set<Site> getSites() {
		return sites;
	}

	public List<Leader> getLeaders() {
		return leaders;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Integer> getChildren() {
		return children;
	}

	public List<EntityLink> getEntityLinks() {
		return entityLinks;
	}

	public EntityPosition getPosition(int id) {
		return positions.get(id);
	}

	public Map<Integer, EntityPosition> getPositions() {
		return positions;
	}

	public EntityPositionAssignment getAssignment(int id) {
		return assignments.get(id);
	}

	public Map<Integer, EntityPositionAssignment> getAssignments() {
		return assignments;
	}

	public List<Integer> getHfIds() {
		return hfIds;
	}

	public List<Integer> getWorshipIds() {
		return worshipIds;
	}

	public List<Coords> getClaims() {
		return claims;
	}

	public boolean isFallen() {
		return fallen;
	}

	public void setFallen(boolean fallen) {
		this.fallen = fallen;
	}

	public static String getColor(String race) {
		switch (race.toLowerCase()) {
		case "kobold":
		case "kobolds":
			return "#333";
		case "goblin":
		case "goblins":
			return "#CC0000";
		case "elf":
		case "elves":
			return "#99FF00";
		case "dwarf":
		case "dwarves":
			return "#FFCC33";
		case "human":
		case "humans":
			return "#0000CC";
		case "necromancer":
		case "necromancers":
			return "#A0A";
		default:
			return "#F0F";
		}
	}

	public void process() {
		// entities that only own towers are shown as necromancers
		if (getSites().stream().filter(s -> "tower".equals(s.getType())).collect(Collectors.counting()) > 0)
			setRace("necromancers");

		// mark sites pillaged to have no remaining population as ruin
		getSites().stream().filter(s -> s.getPopulations().isEmpty()).forEach(s -> s.setRuin(true));

		// mark civilizations that own no sites or only ruins as fallen
		if (type.equals("civilization")) {
			long siteCount = getSites().stream()
					.filter(s -> !s.isRuin() && s.getOwner() != null && this.equals(s.getOwner().getRoot()))
					.collect(Collectors.counting());
			if (siteCount == 0)
				setFallen(true);
		}

	}

	public String getColor() {
		if (id == -1)
			return "#ddf";

		return Entity.getColor(race);
	}

	@Override
	public String toString() {
		return "[" + id + "] " + getName();
	}

	public String getURL() {
		return "/entity/" + id;
	}

	public static String getGlyph(String type) {
		switch (type) {
		case "sitegovernment":
			return "fa fa-balance-scale";
		case "outcast":
			return "glyphicon glyphicon-tent";
		case "nomadicgroup":
			return "glyphicon glyphicon-tree-deciduous";
		case "religion":
			return "fa fa-university";
		case "performancetroupe":
			return "glyphicon glyphicon-cd";
		case "migratinggroup":
			return "glyphicon glyphicon-transfer";

		case "civilization":
		default:
			return "glyphicon glyphicon-star";
		}
	}

	public String getGlyph() {
		if (isFallen())
			return "glyphicon glyphicon-star-empty";
		return getGlyph(type);
	}

	private String getIcon() {
		return "<span class=\"" + getGlyph() + "\" style=\"color: " + getColor() + "\" aria-hidden=\"true\"></span> ";
	}

	public String getLink() {
		if (id == -1)
			return "<i>UNKNOWN ENTITY</i>";

		return "<a href=\"" + getURL() + "\" class=\"entity\">" + getIcon() + getName() + "</a>";
	}

	public List<Entity> getWarEnemies() {
		return World.getHistoricalEventCollections().stream().filter(e -> e instanceof WarCollection)
				.map(e -> (WarCollection) e)
				.filter(e -> e.getAggressorEntId() == getId() || e.getDefenderEntId() == getId()).map(e -> {
					if (e.getAggressorEntId() == getId())
						return e.getDefenderEntId();
					else
						return e.getAggressorEntId();
				}).map(World::getEntity).collect(Collectors.toList());

	}

	public List<WarCollection> getWars() {
		return World.getHistoricalEventCollections().stream().filter(e -> e instanceof WarCollection)
				.map(e -> (WarCollection) e)
				.filter(e -> e.getAggressorEntId() == getId() || e.getDefenderEntId() == getId())
				.collect(Collectors.toList());
	}

	public List<Entity> getGroups() {
		return World.getEntities().stream()
				.filter(e -> !e.equals(this) && (this.equals(e.getParent()) || this.equals(e.getRoot())))
				.collect(Collectors.toList());
	}

	public List<OccasionCollection> getOccasions() {
		return World.getHistoricalEventCollections().stream()
				.collect(Filters.filterCollection(OccasionCollection.class, c -> c.getCivId() == id))
				.collect(Collectors.toList());
	}

}
