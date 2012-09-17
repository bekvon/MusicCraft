/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.musiccraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Administrator
 */
public class MusicCraftPlayerListener implements Listener {

    MusicCraft parent;

    public MusicCraftPlayerListener(MusicCraft in) {
        parent = in;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(parent.isEnabled() && MusicCraft.getManager().stopSongsOnQuit())
        {
            MusicCraft.getManager().removePlayersPerformers(event.getPlayer());
        }
        //super.onPlayerQuit(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
         if (parent.isEnabled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                Player player = event.getPlayer();
                Sign sign = (Sign) block.getState();
                String[] lines = sign.getLines();
                if (lines[0].startsWith("[MML]") || lines[0].equals("[MMLSong]")) {
                    if (MusicCraft.hasAuthority(event.getPlayer(), "musiccraft.use", true)) {
                        Block noteBlock = BlockHelper.findNearbyBlock(block.getWorld(), block.getLocation(), Material.NOTE_BLOCK.getId(), 2);
                        if (noteBlock != null) {
                            NoteBlock playBlock = (NoteBlock) noteBlock.getState();
                            if (MusicCraft.getManager().isBlockPlaying(playBlock)) {
                                MusicCraft.getManager().stopMMLSong(playBlock, player);
                            } else {

                                if (lines[0].equals("[MMLSong]")) {
                                    if (MusicCraft.getManager().getFileManager().getSongExists(lines[1])) {
                                        String MMLString = MusicCraft.getManager().getFileManager().getSongMML(lines[1]);
                                        MusicCraft.getManager().playMMLSong(MMLString, playBlock, player);
                                    } else {
                                        player.sendMessage("§cSong file not found...");
                                    }
                                } else {
                                    String MMLString = lines[0].substring(5).concat(lines[1]).concat(lines[2]).concat(lines[3]);
                                    MusicCraft.getManager().playMMLSong(MMLString, playBlock, player);
                                }
                            }
                        } else {
                            player.sendMessage("§cNo nearby note block found...");
                        }
                    } else {
                        player.sendMessage("§cYou don't have permission...");
                    }
                }
            } else if (block.getType() == Material.NOTE_BLOCK) {
                NoteBlock nblock = (NoteBlock) block.getState();
                if (MusicCraft.getManager().isBlockAssigned(nblock)) {
                    MusicCraft.getManager().stopMMLSong(nblock, event.getPlayer());
                }
            }
        }
    }

}
