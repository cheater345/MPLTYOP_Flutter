import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:cached_network_image/cached_network_image.dart';

import '../../core/constants.dart';
import '../../core/di.dart';
import '../../domain/entities/track.dart';

class PlayerBar extends ConsumerWidget {
  const PlayerBar({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(queueControllerProvider);
    final track = state.currentTrack;

    if (track == null) {
      return const SizedBox.shrink();
    }

    return Container(
      height: 84,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        border: Border(top: BorderSide(color: Colors.grey[800]!)),
      ),
      child: Row(
        children: [
          // Album art
          ClipRRect(
            borderRadius: const BorderRadius.only(
              topLeft: Radius.circular(8),
              bottomLeft: Radius.circular(8),
            ),
            child: CachedNetworkImage(
              imageUrl: track.thumbnailUrl,
              width: 56,
              height: 56,
              fit: BoxFit.cover,
              placeholder: (_, __) => Container(color: Colors.grey[800]),
              errorWidget: (_, __, ___) => Container(
                color: Colors.grey[800],
                child: const Icon(Icons.music_note, color: Colors.grey),
              ),
            ),
          ),
          const SizedBox(width: 12),
          // Track info
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  track.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
                ),
                Text(
                  track.artist,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(color: Colors.grey[400], fontSize: 12),
                ),
              ],
            ),
          ),
          // Controls
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                icon: const Icon(Icons.skip_previous, size: 28),
                onPressed: () => ref.read(queueControllerProvider.notifier).previous(),
              ),
              Consumer(
                builder: (context, ref, _) {
                  final isPlaying = false; // TODO: get from playback state
                  return IconButton(
                    iconSize: 36,
                    icon: Icon(isPlaying ? Icons.pause : Icons.play_arrow),
                    onPressed: () {}, // TODO
                  );
                },
              ),
              IconButton(
                icon: const Icon(Icons.skip_next, size: 28),
                onPressed: () => ref.read(queueControllerProvider.notifier).next(),
              ),
            ],
          ),
          // Volume (web only or expanded)
        ],
      ),
    );
  }
}