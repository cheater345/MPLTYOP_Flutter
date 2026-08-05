import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';

import '../../domain/entities/track.dart';

class TrackTile extends StatelessWidget {
  final Track? track;
  final int index;
  final VoidCallback? onTap;
  final Widget? trailing;

  const TrackTile({
    super.key,
    required this.track,
    required this.index,
    this.onTap,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    if (track == null) {
      return const TrackTileSkeleton();
    }

    return ListTile(
      leading: SizedBox(
        width: 40,
        child: Center(
          child: Text('$index', style: TextStyle(color: Colors.grey[500], fontSize: 14)),
        ),
      ),
      title: Text(
        track!.title,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: const TextStyle(fontWeight: FontWeight.w500),
      ),
      subtitle: Text(
        track!.artist,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(color: Colors.grey[400], fontSize: 12),
      ),
      leading: ClipRRect(
        borderRadius: BorderRadius.circular(4),
        child: CachedNetworkImage(
          imageUrl: track!.thumbnailUrl,
          width: 48,
          height: 48,
          fit: BoxFit.cover,
          placeholder: (_, __) => Container(color: Colors.grey[800]),
          errorWidget: (_, __, ___) => Container(
            color: Colors.grey[800],
            child: const Icon(Icons.music_note, size: 24, color: Colors.grey),
          ),
        ),
      trailing: trailing ??
        PopupMenuButton<String>(
          icon: const Icon(Icons.more_vert, color: Colors.grey),
          onSelected: (value) {},
          itemBuilder: (context) => [
            const PopupMenuItem(value: 'play_next', child: Text('Play next')),
            const PopupMenuItem(value: 'add_queue', child: Text('Add to queue')),
            const PopupMenuItem(value: 'add_playlist', child: Text('Add to playlist')),
            const PopupMenuItem(value: 'like', child: Text('Like')),
            const PopupMenuItem(value: 'share', child: Text('Share')),
          ],
        ),
      onTap: onTap,
    );
  }
}

class TrackTileSkeleton extends StatelessWidget {
  const TrackTileSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Container(width: 48, height: 48, color: Colors.grey[800]),
      title: Container(height: 16, width: 150, color: Colors.grey[800]),
      subtitle: Container(height: 12, width: 100, color: Colors.grey[700]),
    );
  }
}