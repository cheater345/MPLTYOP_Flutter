import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:async/async.dart';

import '../../domain/usecases/search_tracks.dart';
import '../../domain/entities/track.dart';

class SearchController extends StateNotifier<AsyncValue<List<Track>>> {
  final SearchTracks _searchTracks;
  CancelableOperation? _debounce;

  SearchController(this._searchTracks) : super(const AsyncValue.data([]));

  void search(String query) {
    if (query.trim().isEmpty) {
      state = const AsyncValue.data([]);
      return;
    }

    _debounce?.cancel();
    _debounce = CancelableOperation.fromFuture(
      Future.delayed(const Duration(milliseconds: 300)),
      onCancel: () {},
    ).then((_) => _performSearch(query.trim()));
  }

  Future<void> _performSearch(String query) async {
    state = const AsyncValue.loading();
    final result = await _searchTracks(query, limit: 20);
    state = result.fold(
      (failure) => AsyncValue.error(failure, StackTrace.current),
      (tracks) => AsyncValue.data(tracks),
    );
  }

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }
}