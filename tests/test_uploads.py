import io
import os
from unittest.mock import patch

from werkzeug.datastructures import FileStorage

from utils.helpers import save_file


def test_save_file_uses_local_storage_without_supabase(tmp_path, monkeypatch):
    monkeypatch.delenv("SUPABASE_URL", raising=False)
    monkeypatch.delenv("SUPABASE_SERVICE_ROLE_KEY", raising=False)
    upload = FileStorage(stream=io.BytesIO(b"image-data"), filename="pet.png", content_type="image/png")
    stored = save_file(upload, str(tmp_path), "pets")
    assert stored == "pet.png"
    assert (tmp_path / "pet.png").read_bytes() == b"image-data"


def test_save_file_returns_public_supabase_url(monkeypatch):
    monkeypatch.setenv("SUPABASE_URL", "https://example.supabase.co")
    monkeypatch.setenv("SUPABASE_SERVICE_ROLE_KEY", "test-service-key")
    monkeypatch.setenv("SUPABASE_STORAGE_BUCKET", "happy-tails-uploads")
    upload = FileStorage(stream=io.BytesIO(b"image-data"), filename="pet.png", content_type="image/png")

    class Response:
        status = 200
        def __enter__(self): return self
        def __exit__(self, *args): return False

    with patch("utils.helpers.urlrequest.urlopen", return_value=Response()):
        stored = save_file(upload, subfolder="pets")

    assert stored.startswith("https://example.supabase.co/storage/v1/object/public/happy-tails-uploads/pets/")
    assert stored.endswith("-pet.png")
