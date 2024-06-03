namespace ApiAndroid.Entities.DTOs
{
    public class DocumentDto
    {
        public string Title { get; set; } = null!;
        public string FileName { get; set; } = null!;
        public string FileExtension { get; set; } = null!;
        public string Revision { get; set; } = null!;
        public string DocumentFile { get; set; } = null!;
        public int ProductId { get; set; }
    }
}
