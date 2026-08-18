import mimetypes
import os
import uuid
from urllib import request as urlrequest
from werkzeug.utils import secure_filename


def _supabase_configured():
    return bool(os.getenv("SUPABASE_URL") and os.getenv("SUPABASE_SERVICE_ROLE_KEY"))


def save_file(file, upload_folder=None, subfolder="general"):
    """Persist an uploaded file and return the value stored in the database.

    Production uses Supabase Storage when SUPABASE_URL and
    SUPABASE_SERVICE_ROLE_KEY are configured. Local development falls back to
    the existing filesystem behavior.
    """
    filename = secure_filename(file.filename)
    if not filename:
        raise ValueError("Invalid upload filename")

    if _supabase_configured():
        bucket = os.getenv("SUPABASE_STORAGE_BUCKET", "happy-tails-uploads")
        object_name = f"{subfolder}/{uuid.uuid4().hex}-{filename}"
        base_url = os.getenv("SUPABASE_URL").rstrip("/")
        upload_url = f"{base_url}/storage/v1/object/{bucket}/{object_name}"
        content_type = file.mimetype or mimetypes.guess_type(filename)[0] or "application/octet-stream"
        payload = file.read()
        req = urlrequest.Request(
            upload_url,
            data=payload,
            method="POST",
            headers={
                "Authorization": f"Bearer {os.environ['SUPABASE_SERVICE_ROLE_KEY']}",
                "apikey": os.environ["SUPABASE_SERVICE_ROLE_KEY"],
                "Content-Type": content_type,
                "x-upsert": "false",
            },
        )
        with urlrequest.urlopen(req, timeout=20) as response:
            if response.status not in (200, 201):
                raise RuntimeError("Supabase Storage upload failed")
        return f"{base_url}/storage/v1/object/public/{bucket}/{object_name}"

    if not upload_folder:
        raise RuntimeError("Local upload folder is not configured")
    os.makedirs(upload_folder, exist_ok=True)
    file_path = os.path.join(upload_folder, filename)
    file.save(file_path)
    return filename
