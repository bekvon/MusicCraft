/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 *
 * @author Administrator
 */
public class MusicCraftBlockListener implements Listener {

    private MusicCraft parent;

    public MusicCraftBlockListener(MusicCraft in)
    {
        parent = in;
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if(parent.isEnabled() && MusicCraft.getManager().allowRedstone())
        {
            Block block = event.getBlock();
            if(block.getState() instanceof Sign)
            {
                Sign sign = (Sign) event.getBlock().getState();
                if (sign != null) {
                    String[] lines = sign.getLines();
                    if (lines[0].startsWith("[MML]") || lines[0].equals("[MMLSong]")) {
                        Block nb = BlockHelper.findNearbyBlock(block.getWorld(), block.getLocation(), Material.NOTE_BLOCK.getId(), 1);
                        if (nb != null) {
                            NoteBlock noteBlock = (NoteBlock) nb.getState();
                            if (block.isBlockPowered() && !MusicCraft.getManager().isBlockPlaying(noteBlock)) {
                                if (lines[0].equals("[MMLSong]")) {
                                    if (MusicCraft.getManager().getFileManager().getSongExists(lines[1])) {
                                        String MMLString = MusicCraft.getManager().getFileManager().getSongMML(lines[1]);
                                        MusicCraft.getManager().playMMLSong(MMLString, noteBlock);
                                    }
                                } else {
                                    String MMLString = lines[0].substring(5).concat(lines[1]).concat(lines[2]).concat(lines[3]);
                                    MusicCraft.getManager().playMMLSong(MMLString, noteBlock);
                                }
                            } else if (!block.isBlockPowered() && MusicCraft.getManager().isBlockPlaying(noteBlock)) {
                                MusicCraft.getManager().stopMMLSong(noteBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (parent.isEnabled() && !event.isCancelled() && event.getBlock().getType() == Material.NOTE_BLOCK) {
            NoteBlock nblock = (NoteBlock) event.getBlock().getState();
            if (MusicCraft.getManager().isBlockAssigned(nblock)) {
                MusicCraft.getManager().stopMMLSong(nblock, event.getPlayer());
            }
        }
    }

}
