#Advancement Screen Revamped

A client-side overhaul of Minecraft's advancements screen. Instead of hunting through the advancement tree, you get a clear list of advancements and a detail panel that shows exactly which criteria you have completed and which are still missing.
It was built with BlazeandCave's Advancements Pack in mind, where many advancements have long lists of criteria, but it also works with vanilla advancements.

##Features

- Separate advancements.
- screen with tabs per category.
-  List of all advancements you can currently see, with a detail panel on the right.
- Completed and missing criteria per advancement.
- Search and status filter.
- 8 different styles to choose from.
- Favorites.

##How to use

Press L (the default key for advancements) in-game to open the new advancements screen. If you changed that key in the controls menu, use your own key instead.

- Pick a tab at the top to switch between advancement categories.
- Click an advancement in the list on the left to see its details on the right, including which criteria you have completed and which are still missing.
- Use the search box and the status filter to find what you are looking for.
- Easily switch between styles with a button on the advancement screen.

Want to take a look at the original screen? just click "Vanilla screen" and it will open.

##Client-side only

This mod only changes how advancements are displayed. It does not change any requirements, progress or rewards. You do not need it on the server, so it also works on servers where you cannot install mods.

##Known limitations

- Minecraft only sends advancements to the client once they are visible to you. Hidden or locked advancements (and their criteria) cannot be shown until the game reveals them.
- Criteria are shown using their internal names from the advancement files (the JSON criterion name), because writing a readable description for every single criterion by hand is not realistic, especially with BACAP and its large number of criteria. Some names can therefore look technical or cryptic.
