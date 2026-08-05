import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants.dart';
import '../../providers/library_controller.dart';
import '../../../domain/entities/playlist.dart';
import '../../widgets/track_tile.dart';

class PlaylistDetailScreen extends ConsumerWidget {
  final Playlist playlist;
  const PlaylistDetailScreen({super.key, required this.playlist});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 250,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              background: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.grey[800]!, Colors.grey[900]!],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
                child: Center(
                  child: Icon(Icons.queue_music, size: 80, color: Colors.grey[700]),
                ),
              ),
              title: Text(playlist.name),
            ),
            actions: [
              IconButton(icon: const Icon(Icons.play_arrow), onPressed: () {}),
              IconButton(icon: const Icon(Icons.shuffle), onPressed: () {}),
              PopupMenuButton(
                itemBuilder: (context) => [
                  const PopupMenuItem(value: 'edit', child: Text('Edit')),
                  const PopupMenuItem(value: 'delete', child: Text('Delete')),
                ],
                onSelected: (value) {},
              ),
            ],
          ),
          SliverPadding(
            padding: const EdgeInsets.all(16),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  if (index < playlist.trackIds.length) {
                    return TrackTile(
                      track: null, // TODO: load track
                      index: index + 1,
                      onTap: () {},
                      trailing: PopupMenuButton(
                        itemBuilder: (context) => [
                          const PopupMenuItem(value: 'remove', child: Text('Remove')),
                        ],
                        onSelected: (value) {
                          if (value == 'remove') {
                            // TODO: remove from playlist
                          }
                        },
                      ),
                    );
                  }
                  return null;
                },
                childCount: playlist.trackIds.length,
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {}, // TODO: play all
        icon: const Icon(Icons.play_arrow),
        label: const Text('Play'),
      ),
    );
  }
}