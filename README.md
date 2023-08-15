# JTagger
JTagger is a Java library for editing and parsing metadata in audio files. Supports M4A, FLAC, Ogg Vorbis, Ogg Opus, MP3(ID3v1, ID3v1.1, ID3v2.3, ID3v2.4)
# Basic example using generic MediaFile: 
```
MediaFile<AbstractTag, StreamInfo> mediaFile = new MediaFile<>();
mediaFile.scan(new File(mediaPath));
AbstractTag tag = mediaFile.getTag();
StreamInfo info = mediaFile.getStreamInfo();

// print tag information
System.out.printf("%s=%s\n", AbstractTag.TITLE, tag.getStringField(AbstractTag.TITLE));
System.out.printf("%s=%s\n", AbstractTag.ARTIST, tag.getStringField(AbstractTag.ARTIST));
System.out.printf("%s=%s\n", AbstractTag.ALBUM, tag.getStringField(AbstractTag.ALBUM));

// print stream information
int duration = info.getDuration();
System.out.printf("Duration: %02d:%02d\n", duration / 60, duration % 60);
System.out.printf("Sampling rate: %d HZ\n", info.getSampleRate());
System.out.printf("Bitrate: %d kbps\n", info.getBitrate());
System.out.printf("Channels: %d\n", info.getChannelCount());

// set fields
tag.setStringField(AbstractTag.ARTIST, "Roxette");
tag.setStringField(AbstractTag.TITLE, "Neverending Love");
tag.setStringField(AbstractTag.ALBUM, "Pearls of Passion");

// set album cover
AttachedPicture picture = new AttachedPicture();
picture.setPictureData(new File(picturePath)); // accepts URL as well
tag.setPictureField(picture);

// save and close
mediaFile.setTag(tag);
mediaFile.save();
mediaFile.close();
```

