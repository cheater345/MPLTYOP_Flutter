import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';

import '../../domain/entities/track.dart';

class TrackCard extends StatelessWidget {
  final Track track;
  final bool isSectionCard;
  final VoidCallback? onTap;
  final VoidCallback? onPlay;

  const TrackCard({
    super.key,
    required this.track,
    this.isSectionCard = false,
    this.onTap,
    this.onPlay,
  });

  @override
  Widget build(BuildContext context) {
    final width = isSectionCard ? 140.0 : 160.0;
    final imageSize = isSectionCard ? 140.0 : 160.0;

    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: width,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: CachedNetworkImage(
                    imageUrl: track.thumbnailUrl,
                    width: imageSize,
                    height: imageSize,
                    fit: BoxFit.cover,
                    placeholder: (_, __) => Container(
                      width: imageSize,
                      height: imageSize,
                      color: Colors.grey[800],
                    ),
                    errorWidget: (_, __, ___) => Container(
                      width: imageSize,
                      height: imageSize,
                      color: Colors.grey[800],
                      child: const Icon(Icons.music_note, size: 48, color: Colors.grey),
                    ),
                  ),
                ),
                if (!isSectionCard)
                  Positioned(
                    bottom: 8,
                    right: 8,
                    child: Material(
                      color: Theme.of(context).colorScheme.primary,
                      shape: const CircleBorder(),
                      child: InkWell(
                        onTap: onPlay,
                        borderRadius: BorderRadius.circular(24),
                        child: const Padding(
                          padding: EdgeInsets.all(12),
                          child: Icon(Icons.play_arrow, color: Colors.black),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              track.title,
              maxLines: isSectionCard ? 2 : 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
            const SizedBox(height: 4),
            Text(
              track.artist,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(color: Colors.grey[400], fontSize: 12),
            ),
            if (track.duration > 0)
              Text(
                track.formattedDuration,
                style: TextStyle(color: Colors.grey[600], fontSize: 11),
              ),
          ],
        ),
      ),
    );
  }
}