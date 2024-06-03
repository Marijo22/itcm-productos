namespace ApiAndroid.Entities
{
    public class Document
    {
        public int DocumentId { get; set; }
        public string Title { get; set; } = null!;
        public string FileName { get; set; } = null!;
        public string FileExtension { get; set; } = null!;
        public string Revision { get; set; } = null!;
        public string DocumentFile { get; set; } = null!;
        public int ProductId { get; set; }
    }
}
