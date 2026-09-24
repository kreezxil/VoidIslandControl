package com.bartz24.voidislandcontrol.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class IslandPos {
	private int posX;
	private int posY;
	private String type;
	private java.util.HashMap<String, VisitPerms> visitPerms = new java.util.HashMap<String, VisitPerms>();
	private ArrayList<String> playerUUIDs;

	public IslandPos(int x, int y, UUID... ids) {
		posX = x;
		posY = y;
		if (playerUUIDs == null)
			playerUUIDs = new ArrayList<String>();
		for (UUID id : ids) {
			playerUUIDs.add(id.toString());
		}

	}

	public IslandPos(String type, int x, int y, UUID... ids) {
		this.type = type;
		posX = x;
		posY = y;
		if (playerUUIDs == null)
			playerUUIDs = new ArrayList<String>();
		for (UUID id : ids) {
			playerUUIDs.add(id.toString());
		}

	}

	public void addNewPlayer(UUID playerUUID) {
		if (!playerUUIDs.contains(playerUUID.toString()))
			playerUUIDs.add(playerUUID.toString());
	}

	public void removePlayer(UUID playerUUID) {
		if (playerUUIDs.contains(playerUUID.toString()))
			playerUUIDs.remove(playerUUID.toString());
	}

	public int getX() {
		return posX;
	}

	public int getY() {
		return posY;
	}

	public String getType() {
		return type;
	}

	public List<String> getPlayerUUIDs() {
		return playerUUIDs;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("posX", posX);
		nbt.setInteger("posY", posY);
		if (!StringUtils.isEmpty(type))
			nbt.setString("type", type);

		NBTTagList list = new NBTTagList();
		for (int i = 0; i < playerUUIDs.size(); i++) {
			NBTTagCompound stackTag = new NBTTagCompound();

			stackTag.setString("playerUUID", playerUUIDs.get(i));

			list.appendTag(stackTag);
		}
		nbt.setTag("UUIDs", list);

		NBTTagList permList = new NBTTagList();
		for (java.util.Map.Entry<String, VisitPerms> e : visitPerms.entrySet()) {
			NBTTagCompound tag = e.getValue().write();
			tag.setString("playerUUID", e.getKey());
			permList.appendTag(tag);
		}
		nbt.setTag("VisitPerms", permList);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		posX = nbt.getInteger("posX");
		posY = nbt.getInteger("posY");
		type = nbt.getString("type");

		playerUUIDs = new ArrayList<String>();

		NBTTagList list = nbt.getTagList("UUIDs", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound stackTag = list.getCompoundTagAt(i);

			String name = stackTag.getString("playerUUID");
			playerUUIDs.add(name);
		}

		visitPerms = new java.util.HashMap<String, VisitPerms>();
		NBTTagList permList = nbt.getTagList("VisitPerms", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < permList.tagCount(); i++) {
			NBTTagCompound tag = permList.getCompoundTagAt(i);
			visitPerms.put(tag.getString("playerUUID"), VisitPerms.read(tag));
		}
	}

	public VisitPerms getVisitPerms(UUID visitor) {
		return visitPerms.get(visitor.toString());
	}

	public VisitPerms getOrCreateVisitPerms(UUID visitor) {
		VisitPerms p = visitPerms.get(visitor.toString());
		if (p == null) {
			p = VisitPerms.fromConfig();
			visitPerms.put(visitor.toString(), p);
		}
		return p;
	}

	public void setVisitPerm(UUID visitor, String flag, boolean value) {
		getOrCreateVisitPerms(visitor).set(flag, value);
	}
}
