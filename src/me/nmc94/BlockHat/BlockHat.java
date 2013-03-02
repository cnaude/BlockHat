package me.nmc94.BlockHat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockHat extends JavaPlugin
{	
	public static Permission permission;
	public final static String NOPERM = ChatColor.DARK_RED + "You're not allowed to use that command!";
	private final static Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable()
	{
                if (!this.setupPermissions()) 
                {
                    log.info("BlockHat requires Vault. Download the latest version from http://http://dev.bukkit.org/server-mods/vault/");
                    getServer().getPluginManager().disablePlugin(this);
                }
	}

	private Boolean setupPermissions()
	{
                if (getServer().getPluginManager().getPlugin("Vault") != null) 
                {
                    RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                    if (permissionProvider != null)
                    {
                            permission = permissionProvider.getProvider();
                    }
                }
		return (permission != null);
	}

	boolean checkPermission(CommandSender sender, String nodes)
	{
		// TODO: Clean this up.
		if (sender instanceof ConsoleCommandSender)
		{
			return nodes.startsWith("blockhat.hat.give.");
		}

		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			return permission.has(player, nodes);
		}

		return false;
	}

        @Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		Player player = null;
		if (sender instanceof Player)
		{
			player = (Player)sender;
		}

		if (commandLabel.equalsIgnoreCase("hat") || commandLabel.equalsIgnoreCase("blockhat")) 
		{
			if (args.length == 1)
			{
				if (args[0].equals("help"))
				{
					showHelp(command.getName().toLowerCase(), sender);
				}
				else if (args[0].startsWith("ver"))
				{
					sender.sendMessage(this.getDescription().getFullName());
				}
				else if (checkPermission(sender, BlockHatPerm.HAT_ITEMS.node))
				{                                    
					ItemStack stack = stackFromString(args[0], 0);

					if ((stack == null) || (stack.getTypeId() > 255) || (stack.getTypeId() < 1))
					{
						sender.sendMessage(ChatColor.RED + args[0] + " is not a valid block");
						return true;
					}
					placeOnHead(player, stack);
				}
				else
				{
					sender.sendMessage(NOPERM);
				}
			}
			else
			{
				Player other;
				if (args.length == 2)
				{
					if (checkPermission(sender, BlockHatPerm.HAT_GIVE_PLAYERS_ITEMS.node))
					{
						ItemStack stack = stackFromString(args[1], 0);

						if ((stack == null) || (stack.getTypeId() > 255) || (stack.getTypeId() < 1))
						{
							sender.sendMessage(ChatColor.RED + args[1] + " is not a valid block");
							return true;
						}

						List<Player> players = getServer().matchPlayer(args[0]);

						if (players.size() < 1)
						{
							sender.sendMessage(ChatColor.RED + "Could not find player");
						}
						else if (players.size() > 1)
						{
							sender.sendMessage(ChatColor.RED + "More than one player found");
							String msg = "";
							for (Iterator<Player> localIterator = players.iterator(); localIterator.hasNext(); )
							{
								other = (Player)localIterator.next();
								msg = msg + " " + other.getName();
							}
							sender.sendMessage(msg.trim());
						}
						else
						{
							Player other1 = (Player)players.get(0);
							placeOnHead(other1, stack);
							sender.sendMessage("Putting a block on " + other1.getName() + "'s head.");
						}
					}
					else 
					{
						sender.sendMessage(NOPERM);
					}
				}
				else if ((args.length > 2) && (args[0].equalsIgnoreCase("group")))
				{
					if (checkPermission(sender, BlockHatPerm.HAT_GIVE_GROUPS_ITEMS.node))
					{
						ItemStack stack = stackFromString(args[2], 0);

						if ((stack == null) || (stack.getTypeId() > 255) || (stack.getTypeId() < 1))
						{
							sender.sendMessage(ChatColor.RED + args[2] + " is not a valid block");
							return true;
						}

						List<Player> players = new ArrayList<Player>();
						for (Player player2 : Bukkit.getOnlinePlayers())
						{
							if (permission.playerInGroup(player2, args[1]))
							{
								players.add(player2);
							}
						}

						if (players.size() < 1)
						{
							sender.sendMessage(ChatColor.RED + "Could not find any players in " + args[1]);
						}
						else
						{
							for (Player other2 : players)
							{
								placeOnHead(other2, stack);
							}
							sender.sendMessage("Putting blocks on players in " + args[1] + " heads.");
						}
					}
					else
					{
						sender.sendMessage(NOPERM);
					}
				}
				else if (checkPermission(sender, BlockHatPerm.HAT.node))
				{
					placeOnHead(player, player.getItemInHand());
				}
				else
				{
					sender.sendMessage(NOPERM);
				}
			}
		} 
		else if (commandLabel.equalsIgnoreCase("unhat")) 
		{
			if (checkPermission(sender, BlockHatPerm.UNHAT.node)) {
				ItemStack item = new ItemStack(0);
				placeOnHead(player, item);
			} else {
				sender.sendMessage(NOPERM);
			}
		}
		return true;
	}

	private void showHelp(String cmd, CommandSender sender)
	{
		ChatColor nm = ChatColor.BLUE;
		ChatColor ch = ChatColor.LIGHT_PURPLE;
		ChatColor cc = ChatColor.WHITE;
		ChatColor cd = ChatColor.GOLD;
		//ChatColor rd = ChatColor.RED;
		ChatColor ct = ChatColor.YELLOW;
		sender.sendMessage(ch + this.getDescription().getFullName());
		sender.sendMessage(cc + "/" + cmd + " help " + cd + "-" + ct + " Displays help menu");
		sender.sendMessage(cc + "/" + cmd + " version " + cd + "-" + ct + " Displays the current version");
		if (checkPermission(sender, "blockhat.hat")) 
                {
			sender.sendMessage(cc + "/" + cmd + " " + cd + "-" + ct + " Puts the currently held item on your head");
                }
		if (checkPermission(sender, "blockhat.hat.items"))
                {
			sender.sendMessage(cc + "/" + cmd + " [block] " + cd + "-" + ct + " Puts a block with block id on your head");
                }
		if (checkPermission(sender, "blockhat.hat.give.players.items")) 
                {
			sender.sendMessage(cc + "/" + cmd + " [player] [block] " + cd + "-" + ct + " Puts a block on another player");
                }
		if (checkPermission(sender, "blockhat.hat.give.groups.items"))
                {
			sender.sendMessage(cc + "/" + cmd + " group [group] [block] " + cd + "-" + ct + " Puts blocks on all the players in that group");
                }
		sender.sendMessage(cd + "-" + ct + " To remove a hat, just take remove it from the helmet spot in your inventory");
		sender.sendMessage(cd + "-" + ct + " Valid hat item id's are 1-255");
		sender.sendMessage(nm + "Thanks for using BlockHat, I hope you enjoy it!");
		sender.sendMessage(cd + "======================" + ch + "Extensions" + cd + "======================");
		sender.sendMessage(cd + "-" + cc + " GlowHat:" + ct + " Adds the ability for glowstone hats to glow!");
	}

	private boolean placeOnHead(Player player, ItemStack item)
	{
		PlayerInventory inv = player.getInventory();
		ArrayList<Integer> validTypes = new ArrayList<Integer>();
		validTypes.add(298); //Leather helmet
		validTypes.add(302); //Chainmail helmet
		validTypes.add(306); //Iron helmet
		validTypes.add(310); //Diamond helmet
		validTypes.add(314); //Gold helmet
                                                

		// Placing AIR on the head is a safe way to remove the hat.
		//if (item.getType() == Material.AIR)
		//{
		//	player.sendMessage(ChatColor.RED + "Why would you want air on your head?");
		//	return false;
		//}

		int id = item.getTypeId();
                String itemName = item.getType().name().toLowerCase();
		if (!((id >= 0 && id <= 255) || validTypes.contains(id)))
		{
			player.sendMessage(ChatColor.RED + "You can't put "+itemName+" on your head silly!");
			return false;
		}

		ItemStack helmet = inv.getHelmet();
		ItemStack hat = new ItemStack(item.getType(), item.getAmount() < 0 ? item.getAmount() : 1, item.getDurability());
                if (item.getEnchantments() != null) {
                    hat.addEnchantments(item.getEnchantments());
                }
		MaterialData data = item.getData();
		if (data != null)
		{
			hat.setData(item.getData());
		}
		inv.setHelmet(hat);
		if (item.getAmount() > 1)
		{
			item.setAmount(item.getAmount() - 1);
		}
		else
		{			
			inv.removeItem(item);
		}

		if (helmet != null && helmet.getAmount() > 0 && checkPermission(player, BlockHatPerm.HAT_RETURN.node))
		{
			HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack[] { helmet });
			if (!leftover.isEmpty())
			{
				player.sendMessage("Was unable to put the old hat away, dropping it at your feet");

				for (Entry<Integer, ItemStack> e : leftover.entrySet())
				{
					player.getWorld().dropItem(player.getLocation(), (ItemStack)e.getValue());
				}
			}
		}
		if (id == 0) 
		{
			player.sendMessage("No longer wearing a hat!");
		} 
		else
		{
			player.sendMessage("Enjoy your new "+itemName+" hat!");
		}
		return true;
	}

	public ItemStack stackFromString(String item, int count)
	{
                Material material;                
                String itemType = item;
                Byte data = 0;
                Short dmg = 0;
                if (item.contains(":")) 
                {
                    String[] sp = item.split(":"); 
                    try
                    {
                        data = Byte.parseByte(sp[1]);
                    }
                    catch (NumberFormatException nf)
                    {

                    }
                    itemType = sp[0];
                }
		material = Material.matchMaterial(itemType);

		if (material == null) 
                {
                    return null;
                }
                ItemStack itemStack = new ItemStack(material, count, dmg, data);
		return itemStack;
	}
        
}
