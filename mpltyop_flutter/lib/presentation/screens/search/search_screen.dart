import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/constants.dart';
import '../../../core/di.dart';
import '../../../domain/entities/track.dart';
import '../../widgets/track_tile.dart' hide TrackTileSkeleton;
import '../../widgets/loading_skeleton.dart';

class SearchScreen extends ConsumerWidget {
  const SearchScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: SearchAnchor(
          builder: (context, controller) => SearchBar(
            controller: controller,
            hintText: 'Search songs, artists...',
            leading: const Icon(Icons.search),
            onTap: () => controller.openView(),
            onChanged: (_) => ref.read(searchControllerProvider.notifier).search(controller.text),
            onSubmitted: (value) => ref.read(searchControllerProvider.notifier).search(value),
          ),
          suggestionsBuilder: (context, controller) => const [],
        ),
      ),
      body: Consumer(
        builder: (context, ref, _) {
          final searchState = ref.watch(searchControllerProvider);
          return searchState.when(
            data: (tracks) {
              if (tracks.isEmpty) {
                return const Center(child: Text('No results found'));
              }
              return ListView.builder(
                itemCount: tracks.length,
                itemBuilder: (context, index) => TrackTile(
                  track: tracks[index],
                  index: index + 1,
                  onTap: () => _playTrack(tracks[index], tracks, index, context),
                ),
              );
            },
            loading: () => ListView.builder(
              itemCount: 8,
              itemBuilder: (_, __) => const TrackTileSkeleton(),
            ),
            error: (error, _) => Center(child: Text(error.toString())),
          );
        },
      ),
    );
  }

  void _playTrack(Track track, List<Track> tracks, int index, BuildContext context) {
    // TODO: implement playback
  }
}