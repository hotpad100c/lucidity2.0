# 2.0.2
- Fixed unsafe access of block-entity in SelectiveRenderingManager.java
- Selective Rendering: each selected area can now use its own transparency instead of the global
  one. Add `:<0-255>` after an area in the Selected Areas config, e.g. `10,0,10:20,10,20:128`.
- Selective Rendering: a transparency of 0 now skips the geometry entirely instead of drawing it
  fully transparent, so it stays hidden under Iris shaders that ignore alpha.
- Flashback: the transparency keyframe now targets either the global transparency or one specific
  area, chosen from a dropdown. Put each area on its own track to animate them independently.
- Flashback: fixed keyframes being written without a type tag, which made replays fail to load with
  "Unable to determine type of keyframe". Existing broken files are recovered on read.
