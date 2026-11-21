# Icons

Every icon Music draws is one of `MusicIcons`, by what it means, never by its look - the screens', the sheets' and the
players' alike. A screen asks for `MusicIcons.Delete` or `MusicIcons.Play`, and `MusicIcons` decides how that meaning is
drawn. Redrawing an icon, or the whole set, never touches a screen.

## One set

| Where | Drawn as | Used by |
| --- | --- | --- |
| `core/designsystem/.../icon/MusicIcons.kt` | Material's icons, filled, unless the pack draws its own | Everything the app draws: screens, sheets, rows, dialogs, top bars, the music player and the video player |

What Android draws itself - the playback notification, the widgets, the floating window, the download and backup
notifications - takes a drawable, not an `ImageVector`. Those icons are the pack's drawables in
`core/common/src/main/res/drawable`: Material's own glyphs, exported with `IconPackExport`, so they are the same drawings
as the app's. The notification's toggles that cannot be dimmed when off use Material's "on" glyphs - `icon_repeat_on.xml`,
`icon_repeat_one_on.xml`, `icon_shuffle_on.xml` - which are drawables alone. The popup menu's arrows,
`icon_arrow_back.xml` and `icon_keyboard_arrow_right.xml`, are the same for its views, in `core/designsystem`.

Outside the set, and drawn as they are: the launcher icon, the splash, the launcher shortcuts, the notification's
status bar icon (`ic_stat_music`), the update notification's (`ic_update`), and the brand marks on About (`ic_music_mark`,
`ic_github`).

## Redesigning the icons

The icon pack is a set of vector drawables, one per icon, named `icon_` and the `MusicIcons` name in snake case:
`MusicIcons.AddPhotoAlternate` is `icon_add_photo_alternate.xml`. A drawable of that name in any module's `res/drawable`
is drawn in the icon's place, everywhere it appears. So:

1. Export the icons as they are now, as the starting point:

   ```bash
   ICON_PACK_DIR=/path/to/pack ./gradlew :core:designsystem:testDebugUnitTest --tests "*IconPackExport*"
   ```

   It writes `xml/` and `svg/` - one file per `MusicIcons` icon, named as the pack names it.
2. Redraw them in any vector tool, on a 24 by 24 grid, in one colour: whatever shows an icon tints it.
3. Turn each SVG into a vector drawable with Android Studio's Vector Asset tool, keeping its name.
4. Put the drawables in `core/designsystem/src/main/res/drawable` - and those already in
   `core/common/src/main/res/drawable` in their place there, where Android draws them by their resource.

A pack can be partial: an icon it has no drawable for is drawn as Material draws it. An icon that points one way - back,
forward - sets `android:autoMirrored="true"`, so it turns round where text runs right to left.

## Changing the style at once

Without a pack, `MusicIcons` draws Material's filled icons. The style is two lines at the top of `MusicIcons.kt` - its
imports and its `Glyphs` and `MirroredGlyphs` - and changing them redraws every icon not in the pack: `Icons.Outlined`,
`Icons.Rounded` or `Icons.Sharp`, with the matching imports. Export the drawables in `core/common` again in the new style.

## Adding an icon

Add it to `MusicIcons` by what it means - `val Archive: ImageVector by icon { Glyphs.Archive }` - and use it by that name.
Two meanings that look alike today are still two icons, so a pack can tell them apart; one meaning is never two icons.
An icon Android draws by its resource is also exported into `core/common/src/main/res/drawable`.

## Every icon

| Icon | Pack drawable | Drawn without a pack as |
| --- | --- | --- |
| `MusicIcons.Add` | `icon_add.xml` | Material `Add` |
| `MusicIcons.AddPhotoAlternate` | `icon_add_photo_alternate.xml` | Material `AddPhotoAlternate` |
| `MusicIcons.Album` | `icon_album.xml` | Material `Album` |
| `MusicIcons.ArrowBack` | `icon_arrow_back.xml` | Material `ArrowBack`, mirrored |
| `MusicIcons.ArrowForward` | `icon_arrow_forward.xml` | Material `ArrowForward`, mirrored |
| `MusicIcons.ArrowDownward` | `icon_arrow_downward.xml` | Material `ArrowDownward` |
| `MusicIcons.ArrowUpward` | `icon_arrow_upward.xml` | Material `ArrowUpward` |
| `MusicIcons.Article` | `icon_article.xml` | Material `Article`, mirrored |
| `MusicIcons.Artist` | `icon_artist.xml` | Material `Person` |
| `MusicIcons.AspectClassic` | `icon_aspect_classic.xml` | Material `Crop54` |
| `MusicIcons.AspectCrop` | `icon_aspect_crop.xml` | Material `Crop` |
| `MusicIcons.AspectExtraWide` | `icon_aspect_extra_wide.xml` | Material `PanoramaWideAngle` |
| `MusicIcons.AspectFit` | `icon_aspect_fit.xml` | Material `FitScreen` |
| `MusicIcons.AspectOriginal` | `icon_aspect_original.xml` | Material `CropOriginal` |
| `MusicIcons.AspectStretch` | `icon_aspect_stretch.xml` | Material `ZoomOutMap` |
| `MusicIcons.AspectWide` | `icon_aspect_wide.xml` | Material `Crop169` |
| `MusicIcons.AudioFile` | `icon_audio_file.xml` | Material `AudioFile` |
| `MusicIcons.AutoAwesome` | `icon_auto_awesome.xml` | Material `AutoAwesome` |
| `MusicIcons.AutoFixHigh` | `icon_auto_fix_high.xml` | Material `AutoFixHigh` |
| `MusicIcons.AutoRotate` | `icon_auto_rotate.xml` | Material `ScreenRotation` |
| `MusicIcons.Backspace` | `icon_backspace.xml` | Material `Backspace`, mirrored |
| `MusicIcons.Backup` | `icon_backup.xml` | Material `Backup` |
| `MusicIcons.Bedtime` | `icon_bedtime.xml` | Material `Bedtime` |
| `MusicIcons.Block` | `icon_block.xml` | Material `Block` |
| `MusicIcons.Bookmark` | `icon_bookmark.xml` | Material `Bookmark` |
| `MusicIcons.Brightness` | `icon_brightness.xml` | Material `BrightnessMedium` |
| `MusicIcons.BugReport` | `icon_bug_report.xml` | Material `BugReport` |
| `MusicIcons.CalendarMonth` | `icon_calendar_month.xml` | Material `CalendarMonth` |
| `MusicIcons.Check` | `icon_check.xml` | Material `Check` |
| `MusicIcons.CheckBox` | `icon_check_box.xml` | Material `CheckBox` |
| `MusicIcons.CheckBoxOutlineBlank` | `icon_check_box_outline_blank.xml` | Material `CheckBoxOutlineBlank` |
| `MusicIcons.CheckCircle` | `icon_check_circle.xml` | Material `CheckCircle` |
| `MusicIcons.Close` | `icon_close.xml` | Material `Close` |
| `MusicIcons.Cloud` | `icon_cloud.xml` | Material `Cloud` |
| `MusicIcons.CloudDone` | `icon_cloud_done.xml` | Material `CloudDone` |
| `MusicIcons.CloudOff` | `icon_cloud_off.xml` | Material `CloudOff` |
| `MusicIcons.Code` | `icon_code.xml` | Material `Code` |
| `MusicIcons.Collapse` | `icon_collapse.xml` | Material `KeyboardArrowDown` |
| `MusicIcons.Comment` | `icon_comment.xml` | Material `Comment`, mirrored |
| `MusicIcons.ContentCopy` | `icon_content_copy.xml` | Material `ContentCopy` |
| `MusicIcons.ContentCut` | `icon_content_cut.xml` | Material `ContentCut` |
| `MusicIcons.ContentPaste` | `icon_content_paste.xml` | Material `ContentPaste` |
| `MusicIcons.CreateNewFolder` | `icon_create_new_folder.xml` | Material `CreateNewFolder` |
| `MusicIcons.Delete` | `icon_delete.xml` | Material `Delete` |
| `MusicIcons.DeleteSweep` | `icon_delete_sweep.xml` | Material `DeleteSweep` |
| `MusicIcons.Description` | `icon_description.xml` | Material `Description` |
| `MusicIcons.Deselect` | `icon_deselect.xml` | Material `Deselect` |
| `MusicIcons.Details` | `icon_details.xml` | Material `Info` |
| `MusicIcons.DoNotDisturbOn` | `icon_do_not_disturb_on.xml` | Material `DoNotDisturbOn` |
| `MusicIcons.Download` | `icon_download.xml` | Material `Download` |
| `MusicIcons.DragHandle` | `icon_drag_handle.xml` | Material `DragHandle` |
| `MusicIcons.DriveFileRenameOutline` | `icon_drive_file_rename_outline.xml` | Material `DriveFileRenameOutline` |
| `MusicIcons.Edit` | `icon_edit.xml` | Material `Edit` |
| `MusicIcons.EditNote` | `icon_edit_note.xml` | Material `EditNote` |
| `MusicIcons.Equalizer` | `icon_equalizer.xml` | Material `Equalizer` |
| `MusicIcons.Export` | `icon_export.xml` | Material `FileUpload` |
| `MusicIcons.Error` | `icon_error.xml` | Material `Error` |
| `MusicIcons.ExpandLess` | `icon_expand_less.xml` | Material `ExpandLess` |
| `MusicIcons.ExpandMore` | `icon_expand_more.xml` | Material `ExpandMore` |
| `MusicIcons.Extension` | `icon_extension.xml` | Material `Extension` |
| `MusicIcons.FastForward` | `icon_fast_forward.xml` | Material `FastForward` |
| `MusicIcons.Favorite` | `icon_favorite.xml` | Material `Favorite` |
| `MusicIcons.FavoriteBorder` | `icon_favorite_border.xml` | Material `FavoriteBorder` |
| `MusicIcons.Feedback` | `icon_feedback.xml` | Material `Feedback` |
| `MusicIcons.FileOpen` | `icon_file_open.xml` | Material `FileOpen` |
| `MusicIcons.File` | `icon_file.xml` | Material `InsertDriveFile` |
| `MusicIcons.Fingerprint` | `icon_fingerprint.xml` | Material `Fingerprint` |
| `MusicIcons.Folder` | `icon_folder.xml` | Material `Folder` |
| `MusicIcons.FolderOpen` | `icon_folder_open.xml` | Material `FolderOpen` |
| `MusicIcons.FormatListNumbered` | `icon_format_list_numbered.xml` | Material `FormatListNumbered` |
| `MusicIcons.Genre` | `icon_genre.xml` | Material `Category` |
| `MusicIcons.GoToAlbum` | `icon_go_to_album.xml` | Material `Album` |
| `MusicIcons.GoToArtist` | `icon_go_to_artist.xml` | Material `Person` |
| `MusicIcons.GraphicEq` | `icon_graphic_eq.xml` | Material `GraphicEq` |
| `MusicIcons.Group` | `icon_group.xml` | Material `Group` |
| `MusicIcons.HighQuality` | `icon_high_quality.xml` | Material `HighQuality` |
| `MusicIcons.History` | `icon_history.xml` | Material `History` |
| `MusicIcons.Image` | `icon_image.xml` | Material `Image` |
| `MusicIcons.Import` | `icon_import.xml` | Material `FileDownload` |
| `MusicIcons.IndeterminateCheckBox` | `icon_indeterminate_check_box.xml` | Material `IndeterminateCheckBox` |
| `MusicIcons.Info` | `icon_info.xml` | Material `Info` |
| `MusicIcons.Input` | `icon_input.xml` | Material `Input`, mirrored |
| `MusicIcons.KeyboardArrowDown` | `icon_keyboard_arrow_down.xml` | Material `KeyboardArrowDown` |
| `MusicIcons.KeyboardArrowRight` | `icon_keyboard_arrow_right.xml` | Material `KeyboardArrowRight`, mirrored |
| `MusicIcons.KeyboardArrowUp` | `icon_keyboard_arrow_up.xml` | Material `KeyboardArrowUp` |
| `MusicIcons.Language` | `icon_language.xml` | Material `Language` |
| `MusicIcons.LibraryAdd` | `icon_library_add.xml` | Material `LibraryAdd` |
| `MusicIcons.LibraryMusic` | `icon_library_music.xml` | Material `LibraryMusic` |
| `MusicIcons.Link` | `icon_link.xml` | Material `Link` |
| `MusicIcons.LinkOff` | `icon_link_off.xml` | Material `LinkOff` |
| `MusicIcons.Lock` | `icon_lock.xml` | Material `Lock` |
| `MusicIcons.LockOpen` | `icon_lock_open.xml` | Material `LockOpen` |
| `MusicIcons.Login` | `icon_login.xml` | Material `Login`, mirrored |
| `MusicIcons.Lyrics` | `icon_lyrics.xml` | Material `Lyrics` |
| `MusicIcons.Monitor` | `icon_monitor.xml` | Material `Monitor` |
| `MusicIcons.MoreTime` | `icon_more_time.xml` | Material `MoreTime` |
| `MusicIcons.MoreVert` | `icon_more_vert.xml` | Material `MoreVert` |
| `MusicIcons.Muted` | `icon_muted.xml` | Material `VolumeOff`, mirrored |
| `MusicIcons.NewReleases` | `icon_new_releases.xml` | Material `NewReleases` |
| `MusicIcons.Notes` | `icon_notes.xml` | Material `Notes`, mirrored |
| `MusicIcons.Numbers` | `icon_numbers.xml` | Material `Numbers` |
| `MusicIcons.OpenInNew` | `icon_open_in_new.xml` | Material `OpenInNew`, mirrored |
| `MusicIcons.OrientationLandscape` | `icon_orientation_landscape.xml` | Material `StayCurrentLandscape` |
| `MusicIcons.OrientationPortrait` | `icon_orientation_portrait.xml` | Material `StayCurrentPortrait` |
| `MusicIcons.Palette` | `icon_palette.xml` | Material `Palette` |
| `MusicIcons.Pause` | `icon_pause.xml` | Material `Pause` |
| `MusicIcons.PictureInPicture` | `icon_picture_in_picture.xml` | Material `PictureInPictureAlt` |
| `MusicIcons.Play` | `icon_play.xml` | Material `PlayArrow` |
| `MusicIcons.PlayAsAudio` | `icon_play_as_audio.xml` | Material `Headphones` |
| `MusicIcons.PlayCircle` | `icon_play_circle.xml` | Material `PlayCircle` |
| `MusicIcons.PlayNext` | `icon_play_next.xml` | Material `QueuePlayNext` |
| `MusicIcons.Playlist` | `icon_playlist.xml` | Material `QueueMusic`, mirrored |
| `MusicIcons.PlaylistAdd` | `icon_playlist_add.xml` | Material `PlaylistAdd`, mirrored |
| `MusicIcons.PushPin` | `icon_push_pin.xml` | Material `PushPin` |
| `MusicIcons.QueueAdd` | `icon_queue_add.xml` | Material `AddToQueue` |
| `MusicIcons.QueueMusic` | `icon_queue_music.xml` | Material `QueueMusic`, mirrored |
| `MusicIcons.RecordVoiceOver` | `icon_record_voice_over.xml` | Material `RecordVoiceOver` |
| `MusicIcons.Redo` | `icon_redo.xml` | Material `Redo`, mirrored |
| `MusicIcons.Refresh` | `icon_refresh.xml` | Material `Refresh` |
| `MusicIcons.RemoveCircleOutline` | `icon_remove_circle_outline.xml` | Material `RemoveCircleOutline` |
| `MusicIcons.Repeat` | `icon_repeat.xml` | Material `Repeat` |
| `MusicIcons.RepeatOne` | `icon_repeat_one.xml` | Material `RepeatOne` |
| `MusicIcons.RepeatStop` | `icon_repeat_stop.xml` | Material `LooksOne` |
| `MusicIcons.Replay` | `icon_replay.xml` | Material `Replay` |
| `MusicIcons.RestartAlt` | `icon_restart_alt.xml` | Material `RestartAlt` |
| `MusicIcons.Restore` | `icon_restore.xml` | Material `Restore` |
| `MusicIcons.Save` | `icon_save.xml` | Material `Save` |
| `MusicIcons.Schedule` | `icon_schedule.xml` | Material `Schedule` |
| `MusicIcons.Screenshot` | `icon_screenshot.xml` | Material `Screenshot` |
| `MusicIcons.Search` | `icon_search.xml` | Material `Search` |
| `MusicIcons.SearchOff` | `icon_search_off.xml` | Material `SearchOff` |
| `MusicIcons.Security` | `icon_security.xml` | Material `Security` |
| `MusicIcons.SeekForward` | `icon_seek_forward.xml` | Material `PlayArrow` |
| `MusicIcons.SelectAll` | `icon_select_all.xml` | Material `SelectAll` |
| `MusicIcons.Sell` | `icon_sell.xml` | Material `Sell` |
| `MusicIcons.Send` | `icon_send.xml` | Material `Send`, mirrored |
| `MusicIcons.Settings` | `icon_settings.xml` | Material `Settings` |
| `MusicIcons.Share` | `icon_share.xml` | Material `Share` |
| `MusicIcons.ShortText` | `icon_short_text.xml` | Material `ShortText` |
| `MusicIcons.ShowChart` | `icon_show_chart.xml` | Material `ShowChart`, mirrored |
| `MusicIcons.Shuffle` | `icon_shuffle.xml` | Material `Shuffle` |
| `MusicIcons.SkipNext` | `icon_skip_next.xml` | Material `SkipNext` |
| `MusicIcons.SkipPrevious` | `icon_skip_previous.xml` | Material `SkipPrevious` |
| `MusicIcons.Smartphone` | `icon_smartphone.xml` | Material `Smartphone` |
| `MusicIcons.Song` | `icon_song.xml` | Material `MusicNote` |
| `MusicIcons.Sort` | `icon_sort.xml` | Material `Sort`, mirrored |
| `MusicIcons.Speed` | `icon_speed.xml` | Material `Speed` |
| `MusicIcons.Storage` | `icon_storage.xml` | Material `Storage` |
| `MusicIcons.Subject` | `icon_subject.xml` | Material `Subject` |
| `MusicIcons.Subscriptions` | `icon_subscriptions.xml` | Material `Subscriptions` |
| `MusicIcons.Subtitles` | `icon_subtitles.xml` | Material `Subtitles` |
| `MusicIcons.Sync` | `icon_sync.xml` | Material `Sync` |
| `MusicIcons.SyncDisabled` | `icon_sync_disabled.xml` | Material `SyncDisabled` |
| `MusicIcons.SystemUpdate` | `icon_system_update.xml` | Material `SystemUpdate` |
| `MusicIcons.Tab` | `icon_tab.xml` | Material `Tab` |
| `MusicIcons.Terminal` | `icon_terminal.xml` | Material `Terminal` |
| `MusicIcons.ThumbUp` | `icon_thumb_up.xml` | Material `ThumbUp` |
| `MusicIcons.Timer` | `icon_timer.xml` | Material `Timer` |
| `MusicIcons.Title` | `icon_title.xml` | Material `Title` |
| `MusicIcons.TouchApp` | `icon_touch_app.xml` | Material `TouchApp` |
| `MusicIcons.TrendingFlat` | `icon_trending_flat.xml` | Material `TrendingFlat`, mirrored |
| `MusicIcons.TrendingUp` | `icon_trending_up.xml` | Material `TrendingUp`, mirrored |
| `MusicIcons.Tune` | `icon_tune.xml` | Material `Tune` |
| `MusicIcons.Undo` | `icon_undo.xml` | Material `Undo`, mirrored |
| `MusicIcons.Verified` | `icon_verified.xml` | Material `Verified` |
| `MusicIcons.Video` | `icon_video.xml` | Material `Movie` |
| `MusicIcons.VideoFile` | `icon_video_file.xml` | Material `VideoFile` |
| `MusicIcons.VideoLibrary` | `icon_video_library.xml` | Material `VideoLibrary` |
| `MusicIcons.Volume` | `icon_volume.xml` | Material `VolumeUp`, mirrored |
| `MusicIcons.Warning` | `icon_warning.xml` | Material `Warning` |
| `MusicIcons.Wifi` | `icon_wifi.xml` | Material `Wifi` |
| `MusicIcons.WifiOff` | `icon_wifi_off.xml` | Material `WifiOff` |
| `MusicIcons.CoverPlaceholder` | `icon_cover_placeholder.xml` | Music's own |
| `MusicIcons.SelectedBadge` | `icon_selected_badge.xml` | Music's own |
