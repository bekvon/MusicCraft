/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;



/**
 *
 * @author Administrator
 */
public class BlockHelper {
    public static Block findNearbyBlock(World world, Location loc, int blockType, int searchSize)
    {
        int maxX,maxY,maxZ,minX,minY,minZ;
        maxX = loc.getBlockX() + searchSize;
        minX = loc.getBlockX() - searchSize;
        maxY = loc.getBlockY() + searchSize;
        minY = loc.getBlockY() - searchSize;
        maxZ = loc.getBlockZ() + searchSize;
        minZ = loc.getBlockZ() - searchSize;
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = minZ; k <= maxZ; k++) {
                    if(world.getBlockTypeIdAt(i, j, k) == blockType)
                    {
                        return world.getBlockAt(i, j, k);
                    }
                }
            }
        }
        return null;
    }

    public static ArrayList<Block> findAllNearbyBlocks(World world, Location loc, int blockType, int searchSize)
    {
        ArrayList<Block> blockList = new ArrayList<Block>();
        int maxX, maxY, maxZ, minX, minY, minZ;
        maxX = loc.getBlockX() + searchSize;
        minX = loc.getBlockX() - searchSize;
        maxY = loc.getBlockY() + searchSize;
        minY = loc.getBlockY() - searchSize;
        maxZ = loc.getBlockZ() + searchSize;
        minZ = loc.getBlockZ() - searchSize;
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                for (int k = minZ; k <= maxZ; k++) {
                    if (world.getBlockTypeIdAt(i, j, k) == blockType) {
                        blockList.add(world.getBlockAt(i, j, k));
                    }
                }
            }
        }
        return blockList;
    }
}
